package com.karthik.AlertSystem;

import com.opencsv.CSVReader;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class AlertApp extends AbstractVerticle {

    private float CPU_UTIL_RULE = 85;
    private float MEM_UTIL_RULE = 75;
    private float DISK_UTIL_RULE = 60;
    private String[] rules = {
            "CPU_UTILIZATION>" + Float.toString(CPU_UTIL_RULE),
            "MEMORY_UTILIZATION>" + Float.toString(MEM_UTIL_RULE),
            "DISK_UTILIZATION>" + Float.toString(DISK_UTIL_RULE),
    };
    private final int CPU_VIOLATED = 0;
    private final int MEM_VIOLATED = 1;
    private final int DISK_VIOLATED = 2;


    public static void main(String args[]) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new AlertApp());
    }

    @Override
    public void start(Future<Void> startFuture) {
        System.out.println("Application started");

        Router router = Router.router(vertx);
        router.get("/alertapp/checkViolation/").handler(this::checkViolation);
        router.post("/alertapp/checkViolationCSV/").handler(this::checkViolationCSV);
        router.get("/").handler(this::startResponse);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(config().getInteger("http.port", 8080), result -> {
                    if (result.succeeded()) {
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                });
    }

    private String getRulesViolated(int rule) {

        if (rule == 0) return "";

        String response = "";

        if (((rule >> CPU_VIOLATED) & 1) == 1) {
            response = response + rules[0] + ",";
        }
        if (((rule >> MEM_VIOLATED) & 1) == 1) {
            response = response + rules[1] + ",";
        }
        if (((rule >> DISK_VIOLATED) & 1) == 1) {
            response = response + rules[2] + ",";
        }

        return response.substring(0, response.length() - 1);

    }

    private String parseRuleAndGetResult(String serverID, float cpuUtil, float memUtil, float diskUtil) {
        int rules = 0;
        String response = "";
        if (cpuUtil > CPU_UTIL_RULE) {
            rules = rules | (1 << CPU_VIOLATED);
        }
        if (memUtil > MEM_UTIL_RULE) {
            rules = rules | (1 << MEM_VIOLATED);
        }
        if (diskUtil > DISK_UTIL_RULE) {
            rules = rules | (1 << DISK_VIOLATED);
        }

        if (rules == 0) {
            response = response + "No Alert," + serverID;
        } else {
            response += "Alert,";
            response = response + serverID + "," + getRulesViolated(rules);
        }
        return response;
    }

    private void checkViolationCSV(RoutingContext routingContext) {
        routingContext.request().setExpectMultipart(true);
        routingContext.request().uploadHandler(upload -> {

            String fileDir = "csv_uploads/" + System.currentTimeMillis() + "_" + upload.filename();

            upload.exceptionHandler(cause -> {
                routingContext.response().setChunked(true).end("Upload failed");
            });

            upload.endHandler(v -> {
                File file = new File(fileDir);
                if (file.exists()) {
                    try {
                        FileReader fileReader = new FileReader(fileDir);

                        CSVReader reader = new CSVReader(fileReader, ',');

                        ArrayList<String> returnResults = new ArrayList<>();
                        String[] record = null;
                        while ((record = reader.readNext()) != null) {
                            String serverID = "";
                            float cpuUtil = 0f;
                            float memUtil = 0f;
                            float diskUtil = 0f;
                            String response = "";
                            int rules = 0;

                            serverID = record[0];
                            cpuUtil = Float.parseFloat(record[1]);
                            memUtil = Float.parseFloat(record[2]);
                            diskUtil = Float.parseFloat(record[3]);

                            response = parseRuleAndGetResult(serverID, cpuUtil, memUtil, diskUtil);
                            returnResults.add(response);
                        }
                        routingContext.response().setChunked(true).end(Json.encodePrettily(returnResults));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    routingContext.response().setChunked(true).end("Unable to process. Please try again.");
                }
            });

            upload.streamToFileSystem(fileDir);
        });
    }

    private void checkViolation(RoutingContext routingContext) {
        String serverData = routingContext.request().getParam("serverData");
        CSVReader csvReader = new CSVReader(new StringReader(serverData));
        String[] record = null;
        String serverID = "";
        float cpuUtil = 0f;
        float memUtil = 0f;
        float diskUtil = 0f;
        try {
            record = csvReader.readNext();
            serverID = record[0];
            cpuUtil = Float.parseFloat(record[1]);
            memUtil = Float.parseFloat(record[2]);
            diskUtil = Float.parseFloat(record[3]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        routingContext.response()
                .end(parseRuleAndGetResult(serverID, cpuUtil, memUtil, diskUtil));
    }

    private void startResponse(RoutingContext routingContext) {
        routingContext.response()
                .end("Application started");
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop(stopFuture);
        System.out.println("Application stopped");
    }
}
