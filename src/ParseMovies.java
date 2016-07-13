
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.mysql.jdbc.Connection;

public class ParseMovies {

	Document domParse;
	HashMap<String, Integer> movieMap;
	HashMap<String, Integer> starMap;

	public static void main(String[] args)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		// TODO Auto-generated method stub
		final long startTime = System.currentTimeMillis();
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = (Connection) DriverManager.getConnection("jdbc:mysql:///moviedb", args[0], args[1]);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		conn.setAutoCommit(false);

		ParseMovies pm = new ParseMovies();

		System.out.println("Parsing Movie Data from mains243.xml");
		pm.parseMovieXML("mains243.xml");
		pm.insertMovieData(conn);
		System.out.println("Parsing Star Data from actors63.xml");
		pm.parseMovieXML("actors63.xml");
		pm.insertStarData(conn);
		System.out.println("Parsing Stars in Movies Data from casts124.xml");
		pm.parseMovieXML("casts124.xml");
		pm.insertStarMovieData(conn);

		conn.close();
		final long endTime = System.currentTimeMillis();

		System.out.println("Total execution time: " + (endTime - startTime) / 1000.0 + " Seconds");

	}

	public ParseMovies() {
		movieMap = new HashMap<String, Integer>();
		starMap = new HashMap<String, Integer>();
	}

	public void parseMovieXML(String xmlFile) {
		DocumentBuilderFactory docfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docbld;
		try {
			docbld = docfac.newDocumentBuilder();
			domParse = docbld.parse("./" + xmlFile);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void insertMovieData(Connection conn) throws SQLException {

		try {
			Element docElem = domParse.getDocumentElement();
			NodeList dirfilms = docElem.getElementsByTagName("directorfilms");
			String movieTitle = "";
			int movieYear = 1992;
			String movieDirector = "";
			String fid = "Default";
			int movieid = 0;
			int genreid = 0;

			PreparedStatement ptGetMaxId = null;
			ptGetMaxId = (PreparedStatement) conn.prepareStatement("select max(id) from genres");
			ResultSet maxRes = ptGetMaxId.executeQuery();
			if (maxRes.next()) {
				genreid = maxRes.getInt(1);
			}
			ptGetMaxId.close();

			PreparedStatement ptGetMaxIdM = null;
			ptGetMaxIdM = (PreparedStatement) conn.prepareStatement("select max(id) from movies");
			ResultSet maxRes1 = ptGetMaxIdM.executeQuery();
			if (maxRes1.next()) {
				movieid = maxRes1.getInt(1);
			}
			ptGetMaxIdM.close();

			PreparedStatement ptInsertMovie = null;
			PreparedStatement ptInsertGenre = null;
			PreparedStatement checkGenre = null;
			PreparedStatement ptGenreMovie = null;
			ptInsertMovie = (PreparedStatement) conn
					.prepareStatement("Insert into movies (id, title, year, director) Values (? ,?, ?, ?);");
			ptInsertGenre = (PreparedStatement) conn.prepareStatement("Insert into genres (id, name) Values (?, ?);");
			checkGenre = (PreparedStatement) conn.prepareStatement("Select id from genres where name = ?;");
			ptGenreMovie = (PreparedStatement) conn
					.prepareStatement("Insert into genres_in_movies (genre_id, movie_id) Values (?, ?);");

			if (dirfilms.getLength() > 0 && dirfilms != null) {
				for (int i = 0; i < dirfilms.getLength(); i++) {
					Element dirfilm = (Element) dirfilms.item(i);
					NodeList films = dirfilm.getElementsByTagName("films");

					if (films.getLength() > 0 && films != null) {
						for (int j = 0; j < films.getLength(); j++) {
							Element filmElem = (Element) films.item(j);
							NodeList filmList = filmElem.getElementsByTagName("film");

							if (filmList.getLength() > 0 && filmList != null) {
								for (int k = 0; k < filmList.getLength(); k++) {

									// Get Film ID from XML
									Element film = (Element) filmList.item(k);
									NodeList idList = film.getElementsByTagName("fid");
									try {
										fid = idList.item(0).getFirstChild().getNodeValue();
										fid = fid.trim();
									} catch (Exception e) {
										try {
											idList = film.getElementsByTagName("filmed");
											fid = idList.item(0).getFirstChild().getNodeValue();
										} catch (Exception e1) {
											System.out.println("Movie FID not Found");
										}
									}

									// Get Movie Title from XML
									NodeList titleList = film.getElementsByTagName("t");
									try {
										movieTitle = titleList.item(0).getFirstChild().getNodeValue();
									} catch (Exception titleError) {
										// use default movie title
										movieTitle = "Default";
									}

									// Get Movie Year from XML
									NodeList yearList = film.getElementsByTagName("year");
									try {
										movieYear = Integer.parseInt(yearList.item(0).getFirstChild().getNodeValue());
									} catch (Exception e) {
										// use default movie id
										movieYear = 1992;
									}

									// Get Movie Director from XML
									NodeList dirsList = film.getElementsByTagName("dirs");
									try {
										Element dirs = (Element) dirsList.item(0);
										NodeList dirLst = dirs.getElementsByTagName("dir");
										Element dir = (Element) dirLst.item(0);
										NodeList dirnLst = dir.getElementsByTagName("dirn");
										movieDirector = dirnLst.item(0).getFirstChild().getNodeValue();
									} catch (Exception dirFail) {
										// use deafult director name
										movieDirector = "Default";
									}

									movieid++;
									movieMap.put(fid, movieid);

									if (movieTitle == null)
										movieTitle = "Default";

									if (movieDirector == null)
										movieDirector = "Default";

									movieTitle = movieTitle.trim();
									movieDirector = movieDirector.trim();
									ptInsertMovie.setInt(1, movieid);
									ptInsertMovie.setString(2, movieTitle);
									ptInsertMovie.setInt(3, movieYear);
									ptInsertMovie.setString(4, movieDirector);
									ptInsertMovie.addBatch();

									NodeList catsLst = film.getElementsByTagName("cats");
									if (catsLst != null && catsLst.getLength() > 0) {
										Element cats = (Element) catsLst.item(0);
										NodeList catLst = cats.getElementsByTagName("cat");
										if (catLst != null) {
											for (int m = 0; m < catLst.getLength(); m++) {
												NodeList catSubLst = catLst.item(m).getChildNodes();
												if (catSubLst != null && catSubLst.getLength() > 0) {
													String genreName = catSubLst.item(0).getNodeValue();
													if (genreName != null) {
														genreName = genreName.trim();
														checkGenre.setString(1, genreName);
														ResultSet genreData = checkGenre.executeQuery();

														if (genreData.next()) {
															int genId = genreData.getInt(1);
															ptGenreMovie.setInt(1, genId);
														} else {
															genreid++;
															ptInsertGenre.setInt(1, genreid);
															ptInsertGenre.setString(2, genreName);
															ptInsertGenre.executeUpdate();
															ptGenreMovie.setInt(1, genreid);
														}
														ptGenreMovie.setInt(2, movieid);
														ptGenreMovie.addBatch();
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}

			ptInsertMovie.executeBatch();
			ptGenreMovie.executeBatch();
			conn.commit();
			ptInsertMovie.close();
			ptInsertGenre.close();
			ptGenreMovie.close();
		} catch (SQLException sqlExp) {
			System.out.println(sqlExp.getMessage());
		}
	}

	public void insertStarData(Connection conn) throws SQLException {
		try {
			String stgNm = null;
			String fstNm = null;
			String lastNm = null;
			String date = null;
			int starid = 0;
			int flag = 0;

			PreparedStatement ptGetMaxId = null;
			ptGetMaxId = (PreparedStatement) conn.prepareStatement("select max(id) from stars;");
			ResultSet maxRes = ptGetMaxId.executeQuery();
			if (maxRes.next()) {
				starid = maxRes.getInt(1);
			}
			ptGetMaxId.close();

			PreparedStatement ptInsertStar = null;
			ptInsertStar = (PreparedStatement) conn
					.prepareStatement("Insert into stars (id, first_name, last_name, dob) Values (? ,?, ?, ?);");

			Element docElem = domParse.getDocumentElement();
			NodeList actorLst = docElem.getElementsByTagName("actor");
			for (int i = 0; i < actorLst.getLength(); i++) {
				flag = 0;
				stgNm = null;
				fstNm = null;
				lastNm = null;
				date = null;

				Element actorElem = (Element) actorLst.item(i);
				// Get actor Stagename
				NodeList stgNmLst = actorElem.getElementsByTagName("stagename");
				try {
					stgNm = stgNmLst.item(0).getFirstChild().getNodeValue();
					stgNm = stgNm.trim();
					stgNm = stgNm.toLowerCase();
				} catch (Exception e1) {
					// do nothing
				}
				// Get actor first Name
				NodeList fstNmLst = actorElem.getElementsByTagName("firstname");
				try {
					fstNm = fstNmLst.item(0).getFirstChild().getNodeValue();
					fstNm = fstNm.trim();
				} catch (Exception e1) {
					// do nothing
				}

				// Get Actor Last Name
				NodeList lastNmLst = actorElem.getElementsByTagName("familyname");
				try {
					lastNm = lastNmLst.item(0).getFirstChild().getNodeValue();
					lastNm = lastNm.trim();
				} catch (Exception e1) {
					// do nothing
				}

				// Get Actor dob
				NodeList dobList = actorElem.getElementsByTagName("dob");
				try {
					date = dobList.item(0).getFirstChild().getNodeValue();
				} catch (Exception e1) {
					// do nothing
				}

				if (fstNm == null) {
					fstNm = "Default";
				}
				if (lastNm == null) {
					lastNm = fstNm;
					fstNm = "";
				}

				if (date == null) {
					date = "1990-01-01";
				} else {
					date = date + "-01-01";
				}

				try {
					ptInsertStar.setDate(4, java.sql.Date.valueOf(date));
				} catch (Exception e1) {
					date = "1990-01-01";
					ptInsertStar.setDate(4, java.sql.Date.valueOf(date));
				}
				starid++;
				ptInsertStar.setInt(1, starid);
				ptInsertStar.setString(2, fstNm);
				ptInsertStar.setString(3, lastNm);
				ptInsertStar.addBatch();

				if (stgNm != null) {
					starMap.put(stgNm, starid);
				}
			}

			ptInsertStar.executeBatch();
			conn.commit();
			ptInsertStar.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public void insertStarMovieData(Connection conn) throws SQLException {
		try {
			String filmid = null;
			String actorStgNm = null;
			int starId;
			int movieId;
			int flag = 0;
			PreparedStatement ptinsertStarMovie = null;
			ptinsertStarMovie = (PreparedStatement) conn
					.prepareStatement("Insert into stars_in_movies (star_id, movie_id) Values (? ,?);");

			Element docElem = domParse.getDocumentElement();
			NodeList dirfilmsList = docElem.getElementsByTagName("dirfilms");
			for (int i = 0; i < dirfilmsList.getLength(); i++) {
				Element dirfilmsElem = (Element) dirfilmsList.item(i);
				NodeList filmcList = dirfilmsElem.getElementsByTagName("filmc");
				if (filmcList != null) {
					for (int j = 0; j < filmcList.getLength(); j++) {
						Element filmcElem = (Element) filmcList.item(j);
						NodeList mList = filmcElem.getElementsByTagName("m");
						if (mList != null) {
							for (int k = 0; k < mList.getLength(); k++) {
								filmid = null;
								actorStgNm = null;
								flag = 0;
								// count3++;
								Element mElem = (Element) mList.item(k);
								// Get Film ID
								NodeList fList = mElem.getElementsByTagName("f");
								if (fList != null && fList.getLength() > 0) {
									try {
										filmid = fList.item(0).getFirstChild().getNodeValue();
										filmid = filmid.trim();
									} catch (Exception e1) {
										// do nothing
									}
								}

								NodeList aList = mElem.getElementsByTagName("a");
								if (aList != null) {
									try {
										actorStgNm = aList.item(0).getFirstChild().getNodeValue();
										actorStgNm = actorStgNm.trim();
										actorStgNm = actorStgNm.toLowerCase();
									} catch (Exception e2) {
										// do nothing
									}
								}

								if (actorStgNm != null && filmid != null) {

									if (!movieMap.containsKey(filmid)) {
										flag = 1;
									}

									if (!starMap.containsKey(actorStgNm)) {
										flag = 1;
									}

									if (flag == 0) {
										starId = starMap.get(actorStgNm);
										movieId = movieMap.get(filmid);
										ptinsertStarMovie.setInt(1, starId);
										ptinsertStarMovie.setInt(2, movieId);
										ptinsertStarMovie.addBatch();
									}
								}
							}
						}
					}
				}
			}
			ptinsertStarMovie.executeBatch();
			conn.commit();
			ptinsertStarMovie.close();
			// PreparedStatement temp = (PreparedStatement)
			// conn.prepareStatement("ALTER IGNORE TABLE stars_in_movies ADD
			// constraint myuniqueconstaint UNIQUE (star_id, movie_id);");
			// temp.executeUpdate();
			// temp.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
}
