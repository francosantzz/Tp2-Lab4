package org.example;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/lab4apirest";
        String usuario = "root";
        String contraseña = "";
        try {
            Connection conexion = DriverManager.getConnection(url, usuario, contraseña);
            System.out.println("Conexión exitosa a la base de datos");

            for (int codigo = 1; codigo <= 300; codigo++) {
                String endpoint = "https://restcountries.com/v2/callingcode/" + codigo;
                URL urlObj = new URL(endpoint);
                HttpURLConnection conexionHttp = (HttpURLConnection) urlObj.openConnection();
                conexionHttp.setRequestMethod("GET");

                int respuestaCodigo = conexionHttp.getResponseCode();
                if (respuestaCodigo == HttpURLConnection.HTTP_OK) {
                    BufferedReader entrada = new BufferedReader(new InputStreamReader(conexionHttp.getInputStream()));
                    StringBuilder respuesta = new StringBuilder();
                    String linea;
                    while ((linea = entrada.readLine()) != null) {
                        respuesta.append(linea);
                    }
                    entrada.close();

                    JSONArray paises = new JSONArray(respuesta.toString());
                    for (int i = 0; i < paises.length(); i++) {
                        JSONObject paisJSON = paises.getJSONObject(i);
                        if (paisJSON.has("name") && !paisJSON.get("name").equals("") &&
                                paisJSON.has("capital") && !paisJSON.get("capital").equals("") &&
                                paisJSON.has("region") && !paisJSON.get("region").equals("") &&
                                paisJSON.has("population") && !paisJSON.get("population").equals("") &&
                                paisJSON.has("latlng") && !paisJSON.get("latlng").equals("")) {

                            // Obtener datos del JSON
                            String nombrePais = paisJSON.getString("name");
                            int maxLength = 50; // longitud máxima permitida en la columna 'nombrePais'
                            if (nombrePais.length() > maxLength) {
                                nombrePais = nombrePais.substring(0, maxLength);
                            }
                            String capitalPais = paisJSON.getString("capital");
                            String region = paisJSON.getString("region");
                            long poblacion = paisJSON.getLong("population");
                            double latitud = paisJSON.getJSONArray("latlng").getDouble(0);
                            double longitud = paisJSON.getJSONArray("latlng").getDouble(1);
                            int numericCode = paisJSON.getInt("numericCode");

                            // Insertar datos en la tabla Pais
                            String sql ="INSERT INTO Pais (codigoPais, nombrePais, capitalPais, region, poblacion, latitud, longitud) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                                    "ON DUPLICATE KEY UPDATE " +
                                    "nombrePais = VALUES(nombrePais), " +
                                    "capitalPais = VALUES(capitalPais), " +
                                    "region = VALUES(region), " +
                                    "poblacion = VALUES(poblacion), " +
                                    "latitud = VALUES(latitud), " +
                                    "longitud = VALUES(longitud)";
                            PreparedStatement declaracion = conexion.prepareStatement(sql);
                            declaracion.setInt(1, numericCode);
                            declaracion.setString(2, nombrePais);
                            declaracion.setString(3, capitalPais);
                            declaracion.setString(4, region);
                            declaracion.setLong(5, poblacion);
                            declaracion.setDouble(6, latitud);
                            declaracion.setDouble(7, longitud);

                            int filasInsertadas = declaracion.executeUpdate();
                            if (filasInsertadas > 0) {
                                System.out.println("Datos insertados para el código de país: " + codigo);
                            }
                        }else {
                            System.out.println("No se consiguió la capital");
                        }
                    }
                } else {
                    System.out.println("No se pudo obtener información para el código de país: " + codigo);
                }
            }
            conexion.close();
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
