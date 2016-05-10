package webserver;

import com.sun.net.httpserver.*;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WebServer {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(8181), 20);
        HttpContext context = server.createContext("/", new EchoHandler());
        server.setExecutor(null);
        server.start();

    }

    static class EchoHandler implements HttpHandler {

        public void handle(HttpExchange exchange) throws IOException {
            String dirPath = "/";
            String Comm = null;
            String Path = null;
            String path = null;
            String Name = null;

            InputStream in = exchange.getRequestBody();
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            while (true) {
                byte[] buf = new byte[2048];
                int read = 0;
                while ((read = in.read(buf)) != -1) {
                    body.write(buf, 0, read);
                }
                String comm = body.toString();
                JSONParser parser = new JSONParser();
                try {
                    JSONObject Command = (JSONObject) parser.parse(comm);
                    Comm = (Command.get("command").toString());
                    Path = (Command.get("path").toString());
                    Name = (Command.get("name").toString());

                } catch (ParseException ex) {
                    Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
                }

                JSONObject Result = new JSONObject();
                switch (Comm) {
                    case "go":
                        JSONArray List = new JSONArray();
                        dirPath = dirPath + Path;
                        File f = new File(dirPath);
                        File[] files = f.listFiles();
                        for (int i = 0; i < files.length; i++) {
                            JSONObject Ans = new JSONObject();
                            if (files[i].isFile()) {
                                Ans.put("Type", "file");
                            } else {
                                Ans.put("Type", "folder");
                            }
                            path = files[i].toString();
                            Name = path.substring(path.lastIndexOf("\\") + 1);
                            Ans.put("Path", files[i].toString());
                            Ans.put("Name", Name);
                            Ans.put("Id", i + "");
                            List.add("File:" + Ans);
                        }

                        byte[] bytes = List.toString().getBytes();
                        exchange.sendResponseHeaders(200, bytes.length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(bytes);
                        os.close();
                        System.out.println("Каталог " + dirPath + " успешно прочитан");
                        break;
                    case "mkdir":

                        File dir = new File(dirPath + Path + "/" + Name);

                        boolean created = dir.mkdir();
                        if (created) {
                            Result.put("result", "created");
                            System.out.println("Каталог " + Name + " успешно создан");
                        } else {
                            Result.put("result", "no created");
                            System.out.println("Каталог " + Name + " не создан");
                        }
                        byte[] bytesm = Result.toString().getBytes();

                        exchange.sendResponseHeaders(200, bytesm.length);
                        os = exchange.getResponseBody();
                        os.write(bytesm);
                        os.close();
                        break;
                    case "del":
                        dir = new File(dirPath + Path + "/" + Name);

                        boolean deleted = dir.delete();
                        if (deleted) {
                            Result.put("result", "deleted");
                            System.out.println("Каталог " + Name + " успешно удален");
                        } else {
                            Result.put("result", "no deleted");
                            System.out.println("Каталог " + Name + " не удален");
                        }
                        byte[] bytesd = Result.toString().getBytes();

                        exchange.sendResponseHeaders(200, bytesd.length);
                        os = exchange.getResponseBody();
                        os.write(bytesd);
                        os.close();
                        break;
                    default:
                        break;
                }
                System.out.println("------------------------------");
                break;
            }

        }
    }
}
