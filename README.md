# xml-parser-moviedata
Parse movies and actors data from XML for storing them in a DB with below schema.

| Table Name | Attributes| Notes|
| ---------- | --------- | ---- |
| movies     |id:integer (primary key) | title:varchar(100) year:integer | 
director:varchar(100) 
banner_url:varchar(200) 
trailer_url:varchar(200) 
required, AUTO_INCREMENT
required 
required 
required 
URL of movie's "poster"; not required 
URL of trailer; not required
stars
id:integer (primary key)
first_name:varchar(50) 
last_name:varchar(50) 
dob:date 
photo_url:varchar(200) 
required, AUTO_INCREMENT
required 
required 
not required
not required
stars_in_movies
star_id:integer, referencing stars.id
movie_id:integer, referencing movies.id
all attributes required
genres
id:integer (primary key)
name:varchar(32) 
all attributes required; "id" should be 
"AUTO_INCREMENT"
genres_in_movies
genre_id:integer, referencing genres.id
movie_id:integer, referencing movies.id
all attributes required
customers
id:integer (primary key)
first_name:varchar(50) 
last_name:varchar(50) 
cc_id:varchar(20), referencing creditcards.id
address:varchar(200) 
email:varchar(50) 
password:varchar(20) 
all attributes required; "id" should be 
"AUTO_INCREMENT"
sales
id:integer (primary key)
customer_id:integer, referencing customers.id
movie_id:integer, referencing movies.id
sale_date:date 
all attributes required; "id" should be 
"AUTO_INCREMENT"
creditcards
id:varchar(20), (primary key)
first_name:varchar(50) 
last_name:varchar(50) 
expiration:date 
all attributes required


If a movie genre exits in database then it must be linked to the movie via entry in 
