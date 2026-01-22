package com.iprody;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HttpServer {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Server started at http://localhost:8080");

        while (true) {

            Socket clientSocket = serverSocket.accept();

            OutputStream output = clientSocket.getOutputStream();
            PrintWriter headerWriter = new PrintWriter(output, false);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String requestLine = in.readLine();

            if (requestLine != null) {
                String[] parts = requestLine.split(" ");

                if (parts.length >= 2) {
                    String path = parts[1];

                    if (path.equals("/")) {
                        path = "/index.html";
                    }

                    String fileName = path.substring(1);

                    Path base = Paths.get(System.getProperty("user.dir"), "simple-http-server", "static");
                    Path filePath = base.resolve(fileName);


                    if (Files.exists(filePath) && !Files.isDirectory(filePath)) {

                        String contentType = "text/plain";
                        if (fileName.endsWith(".html")) contentType = "text/html";
                        else if (fileName.endsWith(".css")) contentType = "text/css";
                        else if (fileName.endsWith(".js")) contentType = "application/javascript";
                        else if (fileName.endsWith(".png")) contentType = "image/png";

                        byte[] fileBytes = Files.readAllBytes(filePath);

                        headerWriter.println("HTTP/1.1 200 OK");
                        headerWriter.println("Content-Type: " + contentType + "; charset=UTF-8");
                        headerWriter.println("Content-Length: " + fileBytes.length);
                        headerWriter.println();
                        headerWriter.flush();

                        output.write(fileBytes);
                        output.flush();

                        System.out.println("Served: " + fileName);

                    } else {

                        String response = "<h1>404 Not Found</h1>";
                        headerWriter.println("HTTP/1.1 404 Not Found");
                        headerWriter.println("Content-Type: text/html; charset=UTF-8");
                        headerWriter.println("Content-Length: " + response.length());
                        headerWriter.println();
                        headerWriter.flush();

                        output.write(response.getBytes());
                        output.flush();

                        System.out.println("404 Not Found: " + fileName);
                    }
                }
            }

            clientSocket.close();
        }
    }
}