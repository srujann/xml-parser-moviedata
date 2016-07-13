# xml-parser-moviedata
Parse movies and actors data from XML and store them in a MYSQL DB with below schema.

| Table Name | Attributes| Notes|
| ---------- | --------- | ---- |
| movies     | <ul><li>id:integer (primary key)</li><li>title:varchar(100)<li>year:integer</li><li>director:varchar(100)</li><li>banner_url:varchar(200)</li><li>trailer_url:varchar(200)</li> | <ul><li>required, AUTO_INCREMENT</li><li>required</li><li>required</li><li>required</li><li>URL of movie's "poster"; not required</li><li>URL of trailer; not required</li> |
| stars | <ul><li>id:integer (primary key)</li><li>first_name:varchar(50)</li><li>last_name:varchar(50)</li><li>dob:date</li><li>photo_url:varchar(200)</li> | <ul><li>required, AUTO_INCREMENT</li><li>required</li><li>required</li><li>not required</li><li>not required</li></ul> |
| stars_in_movies | <ul><li>star_id:integer, referencing stars.id</li><li>movie_id:integer, referencing movies.id</li>| all attributes required |
| genres | <ul><li>id:integer (primary key)</li><li>name:varchar(32)</li></ul> | <ul><li>all attributes required; "id" should be "AUTO_INCREMENT"</li></ul> |
| genres_in_movies | <ul><li>genre_id:integer, referencing genres.id</li><li>movie_id:integer, referencing movies.id</li></ul>| <ul><li>all attributes required</li></ul> |
| customers | <ul><li>id:integer (primary key)</li><li>first_name:varchar(50)</li><li>last_name:varchar(50)</li><li>cc_id:varchar(20), referencing creditcards.id</li><li>address:varchar(200)</li><li>email:varchar(50)</li><li>password:varchar(20)</li></ul> | <ul><li>all attributes required; "id" should be "AUTO_INCREMENT"</li></ul> |
| sales | <ul><li>id:integer (primary key)</li><li>customer_id:integer, referencing customers.id</li><li>movie_id:integer, referencing movies.id</li><li>sale_date:date</li></ul> | <ul><li>all attributes required; "id" should be "AUTO_INCREMENT"</li></ul> |
| creditcards | <ul><li>id:varchar(20), (primary key)</li><li>first_name:varchar(50)</li><li>last_name:varchar(50)</li><li>expiration:date</li></ul> | <ul><li>all attributes required</li></ul> |

Note:
- Movie data is provided in mains243.xml, stars data is provided in actors63.xml, and stars in a movie data procided in casts124.xml
- If a movie genre encountered, in XML data, is not present in 'genres' table, then a new genre entry must be created
- Movie genre must be linked to the movie through entry in genres_in_movies table
- Each star has stage name and real name. Create entries for stars with their real names
- In casts124.xml maps a movie to star by using an alphanumeric film id of movie obtained from mains243.xml (field not present in movies table, so the mapping of movie to a alphanumeric film id is cached locally), and stage name of star (field not present in stars table, the mapping of star to star stage name is cached locally). Entries for star and movie relation are made in stars_in_movies table.
- Intention is to make as many entries as possible from xml data parsed. Wherever data is missing necessary assumptions are made.

Optimizations Implemented:
- Movies and Stars data are parsed in way that allows batch insert into database
- Movies mapping to a film id and Stars mapping to stage name are cached locally rather than on database to improve speed of execution
