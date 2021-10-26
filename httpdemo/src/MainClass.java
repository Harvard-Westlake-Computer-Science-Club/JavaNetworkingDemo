import java.io.*;
import java.net.*;
import java.util.Scanner;

public class MainClass {

	public static void main(String[] args) {
		
		// In this demo, we'll be learning how to make web (HTTP) requests from our Java programs.
		// This will come in useful for future projects such as our Internet of Things project, where
		// we'll be connecting physical devices to Java programs! This is example code in case you get
		// stuck / don't want to follow along.
		
		// This code uses our HW authentication token to fetch the cafeteria menu for this week. Since
		// HW doesn't have a publicly-accessible API (application programming interface) meant for machines
		// to read, we'll just have to get the webpage HTML that a normal use would get, and parse it using
		// some String operations. This is normally a bad idea, since if the website designers change the
		// design of the site, our code will break; but we don't have a better option right now.
		
		
		
		
		
		// Get the HW authentication token from the user via prompt.
		// You can get your own auth token by:
		// 	1) sign in to hw.com
		//	2) open Chrome developer tools by right clicking anywhere -> "Inspect"
		//	3) click on the Application tab; click on Cookies and choose "https://hw.com"
		//	4) copy the value to the right of the ".DOTNETNUKE" entry
		// Note: giving someone your authentication token is the same as giving them your password (it will give them access to your
		// entire HW account) so don't share it!
		Scanner keyboard = new Scanner(System.in);
		System.out.println("What is your HW authentication token?");
		String token = keyboard.nextLine();
		
		try { // We'll wrap the entire code in a try/catch block to handle any errors that occur.
			
			// Create a URL object and open a connection to the web server.
			URL myUrl = new URL("https://www.hw.com/students/Daily-Life/Cafeteria-Menu");
			HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
			
			// Set the HTTP method to GET (the default one when you navigate to a page in your web browser)
			connection.setRequestMethod("GET");
			
			// Set our Cookie header that includes our authentication info.
			// Remember, headers are small bits of information attached to a packet that contain extra information for the server.
			// A cookie is a small bit of information stored by your browser; in this case, your browser stores the authentication
			// token, and passes it along with every request.
			connection.setRequestProperty("Cookie", "authentication=DNN; .DOTNETNUKE=" + token + ";");
			
			// Get the "response code" of the request. Some common statuses include:
				// 200: everything went OK!
				// 400: malformed / incorrect request data
				// 404: could not find anything at the specified URL
				// 500: something went wrong on the server side
			int status = connection.getResponseCode();
			
			if(status != 200) {
				// If status is not 200 (OK), abort.
				System.out.println("Something went wrong, status: " + status);
				return;
			}
			
			// This is the same Scanner class that we use to read input from the keyboard, but instead of System.in as our input
			// stream, we're using the data the server sent us. Let's loop through it line-by-line and add each to a string.
			Scanner scanner = new Scanner(connection.getInputStream());
			String content = "";
			while (scanner.hasNext()) {
				content += scanner.nextLine() /* + "\n" */;
			}
			
			// To save memory, make sure to clean up.
			scanner.close();
			connection.disconnect();
			
			// If you're familiar with HTML, you know that each "element" has an opening tag, content, and a closing tag:
			// <p>I am a text element!</p>
			// (although some have no content: ) <br />
			
			// Let's find the <table> element on our webpage, which contains our cafeteria menu data. This will let us
			// ignore the extra stuff, like the webpage header and footer.			
			int tableStart = content.indexOf("<tbody>") + 7;
			int tableEnd = content.indexOf("</tbody>") - 1;
			String table = content.substring(tableStart, tableEnd);
			
			// In HTML, a table has rows in its content, and each row has columns:
			// <table>
			//   <tr>
			//     <th>I am a header; Col 1, Row 1!</th>
			//     <th>I am a header; Col 2, Row 1!</th>
			//   </tr>
			//   <tr>
			//	   <th>I am a cell; Col 1, Row 2!</th>
			//     <th>I am a cell; Col 2, Row 2!</th>
			//   </tr>
			// </table>
			
			// Luckily for us, the table on the website has 2 rows for each day, alternating between headers and data, like so:
			// <table>
			//  <tr>Monday</tr>
			//  <tr>Entree: fish soup</tr>
			//  <tr>Tuesday</tr>
			//  <tr>Entree: beef strew</tr>
			//  ...
			// </table>
			
			// Let's loop through our table, parsing row elements into a human-readable string.
			String result = "";
			for(int i = 0; i < 5; i++) {
				// Parse the header row
				// The header rows have a bit of CSS applied, that's what the extra text below is for!
				int tr1Start = table.indexOf("<tr style=\"background-color: #444; color:#f9f9f9;\">") + 53;
				int tr1End = table.indexOf("</tr>");
				if(tr1Start < 0 || tr1End < 0) break;
				String header = table.substring(tr1Start, tr1End);
				table = table.substring(tr1End + 5); // As we process each row, chop it off from the table string.
				// The text is contained in one more layer, a <th> element:
				// 	table -> tr -> th -> "Monday"
				int thStart = header.indexOf("<th>") + 4;
				int thEnd = header.indexOf("</th>");
				String day = header.substring(thStart, thEnd);

				
				// Parse the data row
				int tr2Start = table.indexOf("<tr>") + 4;
				int tr2End = table.indexOf("</tr>");
				String data = table.substring(tr2Start, tr2End);
				table = table.substring(tr2End + 5);  // As we process each row, chop it off from the table string.
				// The text is contained in one more layer, a <td> element:
				//  table -> tr -> td -> "Entree: ..."
				int tdStart = data.indexOf("<td>") + 4;
				int tdEnd = data.indexOf("</td>");
				String text = data.substring(tdStart, tdEnd);
				// And, there are some extra HTML elements. We can erase these from our string.
				text = text.replaceAll("<p>", "");
				text = text.replaceAll("</p>", "");
				text = text.replaceAll("<strong>", "");
				text = text.replaceAll("</strong>", "");
				// Except <br />, which is HTML for "newline"; let's replace this with our Java newline escape character.
				text = text.replaceAll("<br />", "\n");
				
				
				// Finally, add it all to our result string to print out!
				result += "\n\n\n-------\n\n\nOn " + day + ", the cafeteria menu will be:\n" + text;
			}
			
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
