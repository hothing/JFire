
<!-- direct connection allowing DDL -->

	<property name="hibernate.dialect">org.hibernate.dialect.PostgreSQLDialect</property>
    <property name="hibernate.connection.driver_class">${databaseDriverName_noTx}</property>
    <property name="hibernate.connection.url"><![CDATA[${databaseURL}]]></property>
    <property name="hibernate.connection.username">${databaseUserName}</property>
    <property name="hibernate.connection.password"><![CDATA[${databasePassword}]]></property>
