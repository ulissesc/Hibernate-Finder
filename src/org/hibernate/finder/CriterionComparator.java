package org.hibernate.finder;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

public enum CriterionComparator {
	EQ, ILIKE, NOT_EQ, BETWEEN, IN;

	/**
	 * Retorna o comparador adequado para cada situação (tipo de objeto)
	 * 
	 * @param property
	 * @param args
	 * @return
	 */
	public Criterion get(String property, Object... args) {

		/* 2 args para BETWEEN */
		if (BETWEEN == this && args.length != 2)
			throw new IllegalArgumentException(
					"O numero de parametros BETWEEN deve ser igual a 2. Valor: "
							+ args.length);
		
		/* 1 args para os outros */
		else if (BETWEEN != this && args.length != 1) {
			throw new IllegalArgumentException("O numero de parametros do "
					+ this.name() + " deve ser igual a 1. Valor: "
					+ args.length);
		}

		switch (this) {
		case EQ:
			return Restrictions.eq(property, args[0]);
		case ILIKE:
			return Restrictions.ilike(property, 
					String.valueOf(args[0]),
					MatchMode.ANYWHERE);
		case NOT_EQ:
			return Restrictions.ne(property, String.valueOf(args[0]));
		case BETWEEN:
			return Restrictions.between(property, args[0], args[1]);
		default:
			return null;
		}
	}
}
