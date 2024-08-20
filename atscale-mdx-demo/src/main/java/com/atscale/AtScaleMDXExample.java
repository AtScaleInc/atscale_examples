package com.atscale;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

public class AtScaleMDXExample {

    public static void main(String[] args) {
        try {
            // Specify the XMLA endpoint URL (Get this from the Deploy Catalogs page in AtScale Design Center
            String xmlaUrl = "http://localhost/engine/xmla/<XMLA TOKEN>";
            String authServer = "http://localhost";
            String clientSecret = "<CLIENT SECRET>";
            String user = "admin";
            String password = "admin";
            String catalog = "<DEPLOYED CATALOG>";
            String useAggregates = "true";
            String generateAggregates = "true";
            String useQueryCache = "true";
            String useAggregateCache = "true";

            // Specify your MDX query
            String mdxQuery = "SELECT { Measures.[OrderQuantity1] } ON COLUMNS,\n" +
                    "  { DrilldownLevel([Color Dimension].[Color].[All]) } ON ROWS\n" +
                    "FROM [Internet Sales]";

            // Bearer token for authorization
            String bearerToken = getBearerToken(authServer, clientSecret, user, password);

            // Build the SOAP request payload
            String soapRequest = buildSoapRequest(mdxQuery, catalog, useAggregates, generateAggregates, useQueryCache, useAggregateCache);

            // Send the SOAP request
            String response = sendSoapRequest(xmlaUrl, soapRequest, bearerToken);

            // Print the SOAP response
            System.out.println("Response from OLAP server:");
            System.out.println(response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getBearerToken(String authServer, String clientSecret, String user, String password) throws UnirestException {
        Unirest.setTimeouts(0, 0);
        try {
            HttpResponse<JsonNode> response = Unirest.post(authServer + "/auth/realms/atscale/protocol/openid-connect/token")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .field("client_id", "atscale-modeler")
                    .field("client_secret", clientSecret)
                    .field("username", user)
                    .field("password", password)
                    .field("grant_type", "password")
                    .asJson();

            // Check if the request was successful
            if (response.getStatus() == 200) {
                // Parse the JSON response to extract the access token
                JSONObject jsonResponse = response.getBody().getObject();
                String accessToken = jsonResponse.getString("access_token");
                return accessToken;
            } else {
                throw new RuntimeException("Failed to get the token. HTTP Status: " + response.getStatus());
            }
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    private static String buildSoapRequest(String mdxQuery, String catalog, String useAggregates, String generateAggregates, String useQueryCache, String useAggregateCache) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "                  xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "                  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "    <soapenv:Header/>\n" +
                "    <soapenv:Body>\n" +
                "        <Execute xmlns=\"urn:schemas-microsoft-com:xml-analysis\">\n" +
                "            <Command>\n" +
                "                <Statement>" + mdxQuery + "</Statement>\n" +
                "            </Command>\n" +
                "            <Properties>\n" +
                "                <PropertyList>\n" +
                "                    <Catalog>" + catalog + "</Catalog>\n" +
                "                    <UseAggregates>" + useAggregates + "</UseAggregates>\n" +
                "                    <GenerateAggregates>" + generateAggregates + "</GenerateAggregates>\n" +
                "                    <UseQueryCache>" + useQueryCache + "</UseQueryCache>\n" +
                "                    <UseAggregateCache>" + useAggregateCache + "</UseAggregateCache>\n" +
                "                </PropertyList>\n" +
                "            </Properties>\n" +
                "        </Execute>\n" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>";
    }

    private static String sendSoapRequest(String url, String soapRequest, String bearerToken) throws Exception {
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.post(url)
                .header("Content-Type", "application/xml")
                .header("Authorization", "Bearer " + bearerToken)
                .body(soapRequest)
                .asString();
        return response.getBody();
    }
/*    private static String sendSoapRequest(String url, String soapRequest, String bearerToken) throws Exception {
        // Create the connection
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        connection.setRequestProperty("SOAPAction", "urn:schemas-microsoft-com:xml-analysis:Execute");

        // Add the Bearer Token to the Authorization header
        connection.setRequestProperty("Authorization", "Bearer " + bearerToken);

        connection.setDoOutput(true);

        // Send the SOAP request
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(soapRequest.getBytes());
        }

        // Get the SOAP response
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
        }

        return response.toString();
    }*/
}