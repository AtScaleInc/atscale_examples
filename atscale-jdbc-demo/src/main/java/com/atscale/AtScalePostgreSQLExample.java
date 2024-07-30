package com.atscale;

import java.sql.*;

public class AtScalePostgreSQLExample {
    public static void main(String[] args) {
        // Database connection parameters
        String atscale_model = "Internet Sales";
        String atscale_catalog = "sml-internet-sales_main";

        String url = "jdbc:postgresql://localhost:15432/" + atscale_catalog;
        String user = "admin";
        String password = "admin";

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            // Establish the connection
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the AtScale server successfully.");

            // List a column's metdata (in the JDBC REMARKS field)
            //

            // Get database metadata
            DatabaseMetaData metaData = connection.getMetaData();

            // Retrieve columns' metadata for the specified table
            ResultSet columns = metaData.getColumns(atscale_catalog, atscale_catalog, atscale_model, null);

            System.out.println("\nColumn Metadata (JSON):\n");

            // Iterate over the columns and print remarks
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String columnRemark = columns.getString("REMARKS");
                System.out.printf("Column: %-70s Remark: %s%n", columnName, columnRemark);
            }

            // Close the ResultSet
            columns.close();

            // Run a Query and Get Results
            //

            // Create a statement object
            statement = connection.createStatement();

            // Execute a query
            String query = "SELECT \"Internet Sales\".\"CountryCity\" AS \"Country\",\n" + //
                                "  SUM(\"Internet Sales\".\"orderquantity1\") AS \"Order Quantity\"\n" + //
                                "FROM \"" +  atscale_catalog + "\".\"" + atscale_model + "\"\n" + //
                                "GROUP BY 1";
            resultSet = statement.executeQuery(query);

            System.out.println("\nQuery: \n\n" + query);
            System.out.println("\nResults:\n");

            // Process the result set
            while (resultSet.next()) {
                String Country = resultSet.getString("Country");
                Long OrderQuantity = resultSet.getLong("Order Quantity");
                // Fetch other columns as needed
                
                // Display the results
                System.out.println("Country: " + Country + ", Order Quantity: " + OrderQuantity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the resources
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

