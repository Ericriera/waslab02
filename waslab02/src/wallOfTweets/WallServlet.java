package wallOfTweets;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;



@SuppressWarnings("serial")
@WebServlet(urlPatterns = {"/tweets", "/tweets/*"})
public class WallServlet extends HttpServlet {

	private String TWEETS_URI = "/waslab02/tweets/";

	@Override
	// Implements GET http://localhost:8080/waslab02/tweets
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		resp.setContentType("application/json");
		resp.setHeader("Cache-control", "no-cache");
		List<Tweet> tweets= Database.getTweets();
		JSONArray job = new JSONArray();
		for (Tweet t: tweets) {
			JSONObject jt = new JSONObject(t);
			jt.remove("class");
			job.put(jt);
		}
		resp.getWriter().println(job.toString());
	}

	@Override
	// Implements POST http://localhost:8080/waslab02/tweets/:id/likes
	//        and POST http://localhost:8080/waslab02/tweets
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String uri = req.getRequestURI();
		int lastIndex = uri.lastIndexOf("/likes");
		if (lastIndex > -1) {  // uri ends with "/likes"
			// Implements POST http://localhost:8080/waslab02/tweets/:id/likes
			long id = Long.valueOf(uri.substring(TWEETS_URI.length(),lastIndex));		
			resp.setContentType("text/plain");
			resp.getWriter().println(Database.likeTweet(id));
		}
		else { 
			// Implements POST http://localhost:8080/waslab02/tweets
			int max_length_of_data = req.getContentLength();
			byte[] httpInData = new byte[max_length_of_data];
			ServletInputStream  httpIn  = req.getInputStream();
			httpIn.readLine(httpInData, 0, max_length_of_data);
			String body = new String(httpInData);
			/*      ^
		      The String variable body contains the sent (JSON) Data. 
		      Complete the implementation below.*/
			
			try {
				JSONObject obj = new JSONObject(body);
				String author = obj.getString("author");
				String text = obj.getString("text");
				Tweet tw = Database.insertTweet(author, text);
				JSONObject obj2 = new JSONObject(tw);
				obj2.put("token", sha256(String.valueOf(tw.getId())));
				resp.getWriter().println(obj2.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	@Override
	// Implements DELETE http://localhost:8080/waslab02/tweets/:id
	public void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		String uri = req.getRequestURI();
		long id = Long.valueOf(uri.substring(TWEETS_URI.length()));		
		String token = req.getHeader("Authorization");
		
		resp.setContentType("text/plain");
		boolean todook = false;
		if (token.equals(sha256(String.valueOf(id))))todook = Database.deleteTweet(id);
		resp.getWriter().println(todook);
		
		if(!todook)throw new ServletException("DELETE not yet implemented");
	}
	
	public static String sha256(String base) {
	try {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(base.getBytes("UTF-8"));
		StringBuffer hexString = new StringBuffer();

		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}

		return hexString.toString();
	} catch(Exception e) {
		throw new RuntimeException(e);
	}
}

}
