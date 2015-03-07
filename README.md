This is a sample model that uses "databse first" modeling to create a nice
data access library using JOOQ.

To try it out:

 - install postgresql locally or alter code accordingly
 - modify pom.xml, line 121 with your postgresql username (this should go away)
 - createdb sampledb
 - mvn resources:resources liquibase:update compile

The last step above should copy the liquibase config stuff -- including the SQL ddl file
into a spot where the liquibase classloader can find it.  Then, it will invoke the
 JOOQ code generator which will put tons-o DAO code under src/main/generated.

 To Do:

  - write samples that use this non-trivial model to do interesting stuff
  - make it work for other databases?

FYI, my workflow when I created this was:

 - edit the SQL file
 - dropdb sample && createdb sample && mvn resources:resources liquibase:update

This last step will hang/fail if you have a connection open to the database.
