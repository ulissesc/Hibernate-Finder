package org.hibernate.finder;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;


public class HibernateFinder {

	private Session currentSession;
	
	/* DEFAULT true */
	private boolean disjunction = true;
	
	public HibernateFinder(Session currentSession){
		this.currentSession = currentSession;
	}
	
	/**
	 * Informa se a busca generica irá utilizar "disjunction", 
	 * ou seja, utilizará como critério de busca o "OR" (ou) 
	 * entre as consultas. 
	 * <br>Ex.: select * from pessoa where nome = 'ulisses' OR sobre_nome = 'constantini'
	 * 
	 * <br/>
	 * Se  "disjunction" for setada como FALSE, então será utilizado o agregador "AND" (conjunction)
	 * 
	 * <br/><b>Valor default: TRUE</b>
	 * 
	 * @param disjunction
	 */
	public void setDisjunction(boolean disjunction) {
		this.disjunction = disjunction;
	}
	
	
	/**
	 * Informa se a busca generica irá utilizar "disjunction", 
	 * ou seja, utilizará como critério de busca o "OR" (ou) 
	 * entre as consultas. 
	 * <br>Ex.: select * from pessoa where nome = 'ulisses' OR sobre_nome = 'constantini'
	 * 
	 * <br/>
	 * Se  "disjunction" for setada como FALSE, então será utilizado o agregador "AND" (conjunction)
	 * 
	 * <br/><b>Valor default: TRUE</b>
	 * 
	 * @param disjunction
	 */
	public boolean isDisjunction() {
		return disjunction;
	}
	
	/**
	 * Retorna o resultado da busca
	 * 
	 * @param clazz
	 * @param range
	 * @param orderByList
	 * @param valorBusca
	 * @param propriedades
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List find(Class clazz, Range range, List<Order> orderByList, String valorBusca, String... propriedades){
		
		/* CRIA CRITERIO DE BUSCA */
		Criteria criteria = criarCriteriaBusca(
				clazz, 
				valorBusca,
				propriedades);
		
		/* APLICA PAGINACAO, SE HOUVER */
		if (range != null){
			criteria.setFirstResult( range.getFirst() );
			criteria.setMaxResults( range.getMax() );
		}
		
		/* APLICA ORDENACAO, SE HOUVER */
		if (orderByList != null){
			for (Order order : orderByList) {
				criteria.addOrder(order);
			}
		}
		
		return criteria.list();
	}

	/**
	 * Retorna o numero de registros encontrados para determinada busca 
	 * <br/>
	 * É util em situações onde é necessário saber quantos registros foram
	 * encontrados, como por exemplo a utilização na geração da paginação em listagens.
	 * 
	 * @param clazz
	 * @param valorBusca
	 * @param propriedades
	 * @return
	 */
	public int count(@SuppressWarnings("rawtypes") Class clazz, String valorBusca, String... propriedades){
		
		Criteria criteria = criarCriteriaBusca(
				clazz, 
				valorBusca,
				propriedades);
		
		return (Integer) criteria.setProjection(Projections.rowCount()).uniqueResult();
	}
	
	
	@SuppressWarnings("unchecked")
	private Criteria criarCriteriaBusca(@SuppressWarnings("rawtypes") Class clazz,
			String valorBusca, String... propriedades) {
		
		Criteria criteria = currentSession.createCriteria(clazz);
		
		Junction junction = disjunction ? Restrictions.disjunction() : Restrictions.conjunction();
		
		for(String propriedade : propriedades){

			// recupera o valor a ser comparado
			String valor = valorBusca;

			@SuppressWarnings("rawtypes")
			Class propertyType;
			try {
				propertyType = getAnyType(clazz, propriedade);
			} catch (NoSuchFieldException e1) {
				throw new RuntimeException(e1);
			}

			Criterion criterion = null;

			List<String> joins = new ArrayList<String>();
			getJoins(joins, propriedade);
			

			/* REALIZA OS JOINS NECESSARIOS */
			for (String join : joins)
				criteria.createCriteria(join, join);
			
			
			/* propertyType == String */
			if (String.class.equals(propertyType))
				criterion = CriterionComparator.ILIKE.get(propriedade, valor);
			
			
			/* propertyType isInstance java.lang.Number */
			if ( java.lang.Number.class.isInstance( propertyType )) {
				try {
					Object number = propertyType.getMethod("valueOf",
							String.class).invoke(propertyType, valor);
					criterion = CriterionComparator.EQ.get(propriedade, number);
				} catch (Exception e) {
					continue;
				}
			} 

			/* propertyType isInstance java.util.Date */
			if (java.util.Date.class.isInstance( propertyType )) {
				Date data1 = null; 
				try {
					data1 = toSqlDate(valor);
				} catch (Exception e) {
					continue;
				}
				criterion = CriterionComparator.EQ.get(propriedade, data1);
			} 
			
			
			/* propertyType == Timestamp */
			if (Timestamp.class.equals(propertyType)) {
				Timestamp data = null; 
				try {
					data = toSqlTimestampByDate(valor);
				} catch (Exception e) {
					continue;
				}
				criterion = CriterionComparator.EQ.get(propriedade, data);
			}
			
			if (criterion == null)
				continue;
			
			/* SE CRIOU CRITERIO DE BUSCA, ENTAO O ADICIONA */
			junction.add( criterion );
		}
		
		/* ADICIONA A JUNCTION (OR|AND) */
		criteria.add( junction );
		
		return criteria;
	}
	
	
	/**
	 * Retorna os Joins que serao necessários ser feitos para realizar a busca
	 * @param joins
	 * @param propriedade
	 * @return
	 */
	private static final String getJoins(List<String> joins, String propriedade) {
		String[] propiedades = propriedade.split("\\.");
		if (propiedades.length > 1) {
			for (int i = propiedades.length - 2; i >= 0; i--) {
				joins.add(propiedades[i]);
			}
		}
		Collections.reverse(joins);

		return propiedades[propiedades.length - 1];
	}
	
	
	
	/**
	 * Retorna o tipo da propriedade da entidade
	 * 
	 * @param <Entity>
	 * @param clazz
	 * @param propriedade
	 * @return
	 * @throws NoSuchFieldException
	 */
	private static final <Entity> Class<?> getAnyType(Class<Entity> clazz,
			String propriedade) throws NoSuchFieldException {
		String[] propiedades = propriedade.split("\\.");

		@SuppressWarnings("rawtypes")
		Class lastType = null;
		for (String prop : propiedades) {
			if (lastType == null)
				lastType = clazz.getDeclaredField(prop).getType();
			else
				lastType = lastType.getDeclaredField(prop).getType();
		}
		return lastType;
	}
	
	public static Date toSqlDate(String data) throws ParseException {
		if (data == null || data.equals(""))
			return null;

		Date date = null;
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		date = new Date(formatter.parse(data).getTime());
		return date;
	}
	
	public static Timestamp toSqlTimestampByDate(String data) throws ParseException {
		if (data == null || data.equals(""))
			return null;

		Timestamp timestamp = null;
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		timestamp = new Timestamp(formatter.parse(data).getTime());
		return timestamp;
	}
}
