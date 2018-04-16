# JSON-database-local
Java program that creates SQL-like file that is for storing JSON objects on local machine. Program takes STDIN from user input then output to a file that acts as database. Supports add()/get()/delete() functions. 


TECHNOLOGIES: Google's GSON library, JSON, Java
USAGE: 
1.  Download gson jar file to classpath, gson-2.8.2.jar
https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.2/


2. Create file at C://Jason//database.txt 


3. EXAMPLE STDIN:


add {"id":1,"last":"Jason","first":"Chen","major":"Computer Science","address":{"city":"San Jose","state":"CA"}}

//saves the JSON object to database file


get {"address":{"state":"CA"}}

//query database, then prints all people that lives in CA to STDOUT


get {"address":{"state":"CA"}}

//deletes all people that lives in CA


Note: supports nested JSON array but no support for double nested JSON array yet. 
