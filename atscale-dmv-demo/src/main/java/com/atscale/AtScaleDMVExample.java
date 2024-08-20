package com.atscale;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

public class AtScaleDMVExample {

    public static void main(String[] args) {
        try {
            // Specify the XMLA endpoint URL (Get this from the Deploy Catalogs page in AtScale Design Center
            String authServer = "http://localhost";
            String xmlaUrl = "http://localhost/engine/xmla/<XMLA TOKEN>";

            // Auth Info
            String clientSecret = "<CLIENT SECRET>";
            String user = "admin";
            String password = "admin";

            // Query Parameters
            String catalog = "<DEPLOYED CATALOG>";

            // Specify your DMV query
            String dmvQuery = "SELECT [CATALOG_NAME], [SCHEMA_NAME], [CUBE_NAME], [MEASURE_NAME], " +
                    "[MEASURE_UNIQUE_NAME], [MEASURE_GUID], [MEASURE_CAPTION], [MEASURE_AGGREGATOR], " +
                    "[DATA_TYPE], [NUMERIC_PRECISION], [NUMERIC_SCALE], [MEASURE_UNITS], [DESCRIPTION], " +
                    "[EXPRESSION], [MEASURE_IS_VISIBLE], [MEASURE_IS_VISIBLE], [MEASURE_NAME_SQL_COLUMN_NAME], " +
                    "[MEASURE_UNQUALIFIED_CAPTION], [MEASUREGROUP_NAME], [MEASURE_DISPLAY_FOLDER], " +
                    "[DEFAULT_FORMAT_STRING] " +
                    "FROM $system.MDSCHEMA_MEASURES WHERE [CUBE_NAME] = 'Internet Sales'";

            // Bearer token for authorization
            String bearerToken = getBearerToken(authServer, clientSecret, user, password);

            // Build the SOAP request payload
            String soapRequest = buildSoapRequest(dmvQuery, catalog);

            // Send the SOAP request
            String response = sendSoapRequest(xmlaUrl, soapRequest, bearerToken);

            // Print the SOAP response
            System.out.println("Response from AtScale server:");
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

    private static String buildSoapRequest(String dmvQuery, String catalog) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "                  xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "                  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "    <soapenv:Header/>\n" +
                "    <soapenv:Body>\n" +
                "        <Execute xmlns=\"urn:schemas-microsoft-com:xml-analysis\">\n" +
                "            <Command>\n" +
                "                <Statement>" + dmvQuery + "</Statement>\n" +
                "            </Command>\n" +
                "            <Properties>\n" +
                "                <PropertyList>\n" +
                "                    <Catalog>" + catalog + "</Catalog>\n" +
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
}