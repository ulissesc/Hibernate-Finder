Hibernage Generic Finder
========================

Classe Java utilizada para auxiliar a criação de buscas genéricas utilizando o Hibernate.

Utilização
==========

Apenas importe a biblioteca (ou os fontes) para sua aplição. 
Se desejar editar o projeto, o mesmo foi criado utilizando a IDE Eclipse.

E, obviamente, esteja certo que as bibliotecas do Hibernate estejam presente
no classpath da sua aplicação.



Examplos de utilização
======================

        Session session = HibernateUtil.getSession();

		/* passar a session do hibernate */
		HibernateFinder hf = new HibernateFinder( session );
		
		/* busca pelo objeto Pessoa, com paginação e ordenação */
		List<Pessoa> pessoas = hf.find(
				Pessoa.class, /* tipo da entidade */
				new Range(0, 10), /* paginação ou null */
				Order.asc("nome"), /* ordenação ou null */
				"ulisses", /* valor da busca - Pode ser qquer coisa. Ex: string, data, numeros... */
				"nome", /* propriedades a serem buscadas. Coloque qtas quiser. Ex.: "nome" e "sobrenome" */
				"sobrenome"
			);
		
		/* count do resultado */
		int count = hf.count(
				Pessoa.class, /* tipo da entidade */
				new Range(0, 10), /* paginação ou null */
				Order.asc("nome"), /* ordenação ou null */
				"ulisses", /* valor da busca - Pode ser qquer coisa. Ex: string, data, numeros... */
				"nome", /* propriedades a serem buscadas. Coloque qtas quiser. Ex.: "nome" e "sobrenome" */
				"sobrenome"
			);


_Copyright (c) 2010 Ulisses Constantini, released under the MIT license_