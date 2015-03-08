This is a sample model that uses "databse first" modeling to create a nice
data access library using JOOQ.

To try it out:

 - install postgresql locally or alter code accordingly
 - createdb sampledb
 - cp env.sh.sample env.sh # and then modify accordingly
 - . env.sh # load the environment variables
 - mvn resources:resources liquibase:update compile

The last step above should do the following:
 - copy resources (including the SQL ddl file) into a spot where they can be found at runtime by liquibase
 - invokes the JOOQ code generator, which will put lots of code under src/main/generated.

To Do:

  - write samples that use this non-trivial model to do interesting stuff
  - make it work for other databases?

FYI, my workflow when I created this was:

 - edit the SQL file
 - dropdb sample && createdb sample && mvn clean resources:resources liquibase:update test

This last step will hang/fail if you have a connection open to the database.
