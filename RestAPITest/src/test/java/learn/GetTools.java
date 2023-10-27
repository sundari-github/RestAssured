package learn;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.*;
import io.restassured.response.Response;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.List;

public class GetTools {
	
	String accessToken = null;
	Integer toolIdIndex = new Random ().nextInt (20);
	String toolId = null;
	String toolCategory = null;
	
	/* Adding extra comments to check if it will picked up by git as a change */
	
	@BeforeTest
	void getTools ()
	{
		/** GET REQUEST ; CHECK RESPONSE CODE ; READ RESPONSE JSON **/
		Response response = get("https://simple-tool-rental-api.glitch.me/tools"); 						// PLAIN GET REQUEST
		System.out.println ("Response as String " + response.asString());
		System.out.println ("Response Content Type " + response.contentType());
		System.out.println ("Response Status Code " + response.getStatusCode());
		int statusCode = response.getStatusCode();														// STATUS CODE FROM RESPONSE
		Assert.assertEquals(statusCode, 200);														// ASSERT AGAINST RESPONSE CODE
		List<String> ids = response.jsonPath ().get ("id");												// READING OUTPUT JSON
		System.out.println ("Number of responses " + ids.size ());
		
		/** GENERATING RANDOM NUMBER TO IDENTIFY A TOOL INDEX **/
		System.out.println ("toolIdIndex " + toolIdIndex);
		String idParam = new String ("id [").concat(toolIdIndex.toString()).concat("]");
		System.out.println (idParam);
		toolId = response.jsonPath ().getString (idParam);												// READING A SPECIFIC STRING FROM RESPONSE
		System.out.println (toolId);
		String categoryParam = new String ("category [").concat(toolIdIndex.toString()).concat("]");
		toolCategory = response.jsonPath().getString(categoryParam);									// READING A SPECIFIC STRING FROM RESPONSE
		System.out.println (toolCategory);
		
		/** POST REQUEST **/
		Map <String, Object> authTokenRequestMap = new HashMap <String, Object> ();						// THROUGH HASHMAP TO JSONOBJECT
		authTokenRequestMap.put("clientName", "Rest Assured");
		authTokenRequestMap.put("clientEmail", "random17@email.com");
		JSONObject request = new JSONObject (authTokenRequestMap);
		System.out.println ("Request Map " + request.toJSONString());
		accessToken = given ().
							contentType("application/json").											// SETTING CONTENT TYPE
							body (request.toJSONString()).												// SETTING REQUEST BODY
					  when ().
					  		post ("https://simple-tool-rental-api.glitch.me/api-clients").
					  then ().
					  		statusCode (201).
					  extract ().
					  		path("accessToken");														// ACCESSING RESPONSE JSON
		System.out.println ("Access Token " + accessToken);
	}
	
	//@Test
	void getLadders ()
	{
		given ().
			get ("https://simple-tool-rental-api.glitch.me/tools?category=ladders").					// GET REQUEST WITH BDD
		then ().
			statusCode(200).																			// STATUS CODE CHECK
			body ("id [0]", equalTo (2177)).															// OUTPUT JSON VALUE CHECK
			body ("category", hasItems ("ladders")).
			log ().all ();																				// LOGS FULL RESPONSE
		
	}
	//@Test
	void getSingleTool ()
	{
		String singleToolUrl = new String ("https://simple-tool-rental-api.glitch.me/tools/").concat (toolId);
		System.out.println ("single tool url " + singleToolUrl);
		given ().
			get (singleToolUrl).
		then ().
			statusCode (200).
			body ("category", equalTo(toolCategory)).
			log ().all ();
	}
	@Test
	void crudOnOrder ()
	{
		JSONObject createOrderRequest = new JSONObject ();															// CREATING REQUEST JSON DIRECTLY WITH JSONOBJECT
		createOrderRequest.put("toolId", Integer.parseInt(toolId));
		createOrderRequest.put("customerName", "sundari");
		System.out.println (createOrderRequest.toJSONString());
		
		/** CREATE ORDER **/
		Response createOrderResponse = given ().
								contentType("application/json").
								header ("Authorization","Bearer "+accessToken).							// SETTING HEADER FOR REQUEST
								body (createOrderRequest.toJSONString()).
							when ().
					  			post ("https://simple-tool-rental-api.glitch.me/orders");				// SUBSTITUTING post WITH put WITH SIMILAR REQUEST PAYLOAD WILL WORK AS EXPECTED FOR UPDATES
		System.out.println ("Response as String " + createOrderResponse.asString());
		System.out.println ("Response Content Type " + createOrderResponse.contentType());
		System.out.println ("Response Status Code " + createOrderResponse.getStatusCode());
		Assert.assertEquals(createOrderResponse.statusCode(), 201);
		Assert.assertEquals(createOrderResponse.jsonPath().getBoolean("created"), true);
		String orderId = createOrderResponse.jsonPath().getString("orderId");
		System.out.println ("Order ID " + orderId);
		
		/** UPDATE ORDER **/
		JSONObject updateOrderRequest = new JSONObject ();
		updateOrderRequest.put("customerName","Sundari");
		updateOrderRequest.put("comment","From RestAssured");
		System.out.println (updateOrderRequest.toJSONString());
		String postUrl = "https://simple-tool-rental-api.glitch.me/orders/"+orderId;
		System.out.println (postUrl);
		Response updateOrderResponse = given ().
								contentType ("application/json").
								header ("Authorization","Bearer "+accessToken).
								body (updateOrderRequest.toJSONString()).
							when ().
								patch (postUrl);														// PATCH REQUEST FOR UPDATES
		System.out.println ("Response as String " + updateOrderResponse.asString());
		System.out.println ("Response Content Type " + updateOrderResponse.contentType());
		System.out.println ("Response Status Code " + updateOrderResponse.getStatusCode());
		
		/** GET UPDATED ORDER **/
		given ().
			header ("Authorization","Bearer "+accessToken).
		when ().
			get ("https://simple-tool-rental-api.glitch.me/orders/"+orderId).
		then ().
			statusCode (200).
			body ("toolId",equalTo (Integer.parseInt(toolId))).
			body ("customerName", equalTo ("Sundari")).
			body ("comment" , equalTo ("From RestAssured")).
			log (). all ();
		
		/** DELETE ORDER **/
		given ().
			header ("Authorization","Bearer "+accessToken).
		when ().
			delete ("https://simple-tool-rental-api.glitch.me/orders/"+orderId).
		then ().
			statusCode (204);
		System.out.println ("Order deleted successfully");
	}			  			
		
	
	/*
	 *	Header and Parameters can be set before the get
	 * 
	 *	given (). header ("name","value"). parameter ("name", "value"). get ...
	 * 
	 */
	 
	/* 
	 * Request Json can be created directly from JSONObject
	 * 
	 * JSONObject request = new JSONObject (); request.put ("key","value");
	 *  
	 * Response response = given ().
	 * 							contentType("application/json").
	 * 							body(request.toJSONString()).
	 * 						when ().
	 * 							post ("https://simple-tool-rental-api.glitch.me/api-clients"); 
	 * 	System.out.println("Response as String " + response.asString()); 
	 * 	System.out.println("Response Content Type " + response.contentType()); 
	 *  System.out.println("Response Status Code " + response.getStatusCode());
	 * 
	 */
	
	/*
	 * 
	 * 
	 */
	 
	

}
