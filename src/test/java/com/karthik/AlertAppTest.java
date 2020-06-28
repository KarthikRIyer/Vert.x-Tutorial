package com.karthik;

import com.karthik.AlertSystem.AlertApp;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.multipart.MultipartForm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class AlertAppTest {

    private Vertx vertx;

    private int port = 8080;

    @Before
    public void setup(TestContext testContext) {
        vertx = Vertx.vertx();

        vertx.deployVerticle(AlertApp.class.getName(), testContext.asyncAssertSuccess());

    }

    @After
    public void tearDown(TestContext testContext) {
        vertx.close(testContext.asyncAssertSuccess());
    }

    @Test
    public void startedSuccessTest(TestContext testContext) {
        Async async = testContext.async();

        vertx.createHttpClient()
                .getNow(port, "localhost", "/", response -> {
                    response.handler(responseBody -> {
                        testContext.assertTrue(responseBody.toString().contains("Application started"));
                        async.complete();
                    });
                });
    }

    @Test
    public void csvStringTest(TestContext testContext) {
        Async async = testContext.async();

        WebClient client = WebClient.create(vertx);

        client
                .get(port, "localhost", "/alertapp/checkViolation/")
                .addQueryParam("serverData", "10021,63,92,53")
                .send(ar -> {
                    if (ar.succeeded()) {
                        System.out.println(ar.result().bodyAsString());
                        testContext.assertTrue(ar.result().bodyAsString().equals("Alert,10021,MEMORY_UTILIZATION>75.0"));
                    } else {
                        System.out.println("Error fetching results");
                    }

                });
        client
                .get(port, "localhost", "/alertapp/checkViolation/")
                .addQueryParam("serverData", "10022,93,92,53")
                .send(ar -> {
                    if (ar.succeeded()) {
                        System.out.println(ar.result().bodyAsString());
                        testContext.assertTrue(ar.result().bodyAsString().equals("Alert,10022,CPU_UTILIZATION>85.0,MEMORY_UTILIZATION>75.0"));
                    } else {
                        System.out.println("Error fetching results");
                    }
                });
        client
                .get(port, "localhost", "/alertapp/checkViolation/")
                .addQueryParam("serverData", "10023,93,92,93")
                .send(ar -> {
                    if (ar.succeeded()) {
                        System.out.println(ar.result().bodyAsString());
                        testContext.assertTrue(ar.result().bodyAsString().equals("Alert,10023,CPU_UTILIZATION>85.0,MEMORY_UTILIZATION>75.0,DISK_UTILIZATION>60.0"));
                    } else {
                        System.out.println("Error fetching results");
                    }
                });
        client
                .get(port, "localhost", "/alertapp/checkViolation/")
                .addQueryParam("serverData", "10024,13,12,3")
                .send(ar -> {
                    if (ar.succeeded()) {
                        System.out.println(ar.result().bodyAsString());
                        testContext.assertTrue(ar.result().bodyAsString().equals("No Alert,10024"));
                    } else {
                        System.out.println("Error fetching results");
                    }
                    async.complete();
                });
    }

    @Test
    public void csvFileTest(TestContext testContext) {
        Async async = testContext.async();

        MultipartForm form = MultipartForm.create()
                .binaryFileUpload("file", "vals.csv", "vals.csv", "text/csv");

        WebClient client = WebClient.create(vertx);

        String resultCompare = "[ \"Alert,10021,MEMORY_UTILIZATION>75.0\", \"Alert,10022,MEMORY_UTILIZATION>75.0,DISK_UTILIZATION>60.0\", \"Alert,10023,MEMORY_UTILIZATION>75.0\", \"Alert,10024,MEMORY_UTILIZATION>75.0\", \"Alert,10025,DISK_UTILIZATION>60.0\", \"Alert,10026,MEMORY_UTILIZATION>75.0,DISK_UTILIZATION>60.0\", \"Alert,10027,MEMORY_UTILIZATION>75.0,DISK_UTILIZATION>60.0\", \"Alert,10028,CPU_UTILIZATION>85.0\", \"No Alert,10029\", \"No Alert,10030\", \"Alert,10031,DISK_UTILIZATION>60.0\", \"Alert,10032,DISK_UTILIZATION>60.0\", \"Alert,10033,DISK_UTILIZATION>60.0\", \"No Alert,10034\", \"Alert,10035,DISK_UTILIZATION>60.0\", \"Alert,10036,MEMORY_UTILIZATION>75.0\", \"Alert,10037,MEMORY_UTILIZATION>75.0\", \"Alert,10038,DISK_UTILIZATION>60.0\", \"Alert,10039,DISK_UTILIZATION>60.0\", \"Alert,10040,DISK_UTILIZATION>60.0\", \"Alert,10041,DISK_UTILIZATION>60.0\", \"Alert,10042,MEMORY_UTILIZATION>75.0,DISK_UTILIZATION>60.0\", \"Alert,10043,MEMORY_UTILIZATION>75.0\", \"Alert,10044,CPU_UTILIZATION>85.0\", \"Alert,10045,CPU_UTILIZATION>85.0,DISK_UTILIZATION>60.0\", \"Alert,10046,DISK_UTILIZATION>60.0\", \"Alert,10047,CPU_UTILIZATION>85.0,MEMORY_UTILIZATION>75.0,DISK_UTILIZATION>60.0\", \"Alert,10048,DISK_UTILIZATION>60.0\", \"Alert,10049,CPU_UTILIZATION>85.0\", \"Alert,10050,MEMORY_UTILIZATION>75.0,DISK_UTILIZATION>60.0\", \"No Alert,10051\", \"No Alert,10052\", \"No Alert,10053\", \"Alert,10054,CPU_UTILIZATION>85.0\", \"Alert,10055,MEMORY_UTILIZATION>75.0\", \"No Alert,10056\", \"No Alert,10057\", \"Alert,10058,CPU_UTILIZATION>85.0\", \"Alert,10059,CPU_UTILIZATION>85.0\", \"No Alert,10060\", \"Alert,10061,MEMORY_UTILIZATION>75.0,DISK_UTILIZATION>60.0\", \"Alert,10062,CPU_UTILIZATION>85.0\", \"Alert,10063,MEMORY_UTILIZATION>75.0,DISK_UTILIZATION>60.0\", \"No Alert,10064\", \"Alert,10065,MEMORY_UTILIZATION>75.0\", \"Alert,10066,MEMORY_UTILIZATION>75.0\", \"Alert,10067,MEMORY_UTILIZATION>75.0\", \"No Alert,10068\", \"No Alert,10069\", \"No Alert,10070\", \"No Alert,10071\", \"No Alert,10072\", \"Alert,10073,CPU_UTILIZATION>85.0\", \"No Alert,10074\", \"Alert,10075,MEMORY_UTILIZATION>75.0\", \"Alert,10076,MEMORY_UTILIZATION>75.0,DISK_UTILIZATION>60.0\", \"Alert,10077,MEMORY_UTILIZATION>75.0\", \"No Alert,10078\", \"Alert,10079,MEMORY_UTILIZATION>75.0,DISK_UTILIZATION>60.0\", \"Alert,10080,CPU_UTILIZATION>85.0,MEMORY_UTILIZATION>75.0\", \"Alert,10081,DISK_UTILIZATION>60.0\", \"Alert,10082,MEMORY_UTILIZATION>75.0\", \"No Alert,10083\", \"No Alert,10084\", \"Alert,10085,CPU_UTILIZATION>85.0\", \"Alert,10086,CPU_UTILIZATION>85.0\", \"Alert,10087,MEMORY_UTILIZATION>75.0\", \"Alert,10088,MEMORY_UTILIZATION>75.0\", \"No Alert,10089\", \"Alert,10090,DISK_UTILIZATION>60.0\", \"Alert,10091,DISK_UTILIZATION>60.0\", \"Alert,10092,DISK_UTILIZATION>60.0\", \"No Alert,10093\", \"Alert,10094,CPU_UTILIZATION>85.0,DISK_UTILIZATION>60.0\", \"Alert,10095,MEMORY_UTILIZATION>75.0,DISK_UTILIZATION>60.0\", \"No Alert,10096\", \"Alert,10097,DISK_UTILIZATION>60.0\", \"Alert,10098,MEMORY_UTILIZATION>75.0\", \"Alert,10099,MEMORY_UTILIZATION>75.0\", \"Alert,10100,MEMORY_UTILIZATION>75.0,DISK_UTILIZATION>60.0\", \"Alert,10101,DISK_UTILIZATION>60.0\", \"Alert,10102,CPU_UTILIZATION>85.0,DISK_UTILIZATION>60.0\", \"Alert,10103,DISK_UTILIZATION>60.0\", \"Alert,10104,MEMORY_UTILIZATION>75.0\", \"Alert,10105,MEMORY_UTILIZATION>75.0\", \"Alert,10106,DISK_UTILIZATION>60.0\", \"No Alert,10107\", \"Alert,10108,DISK_UTILIZATION>60.0\", \"Alert,10109,DISK_UTILIZATION>60.0\", \"Alert,10110,DISK_UTILIZATION>60.0\", \"Alert,10111,MEMORY_UTILIZATION>75.0,DISK_UTILIZATION>60.0\", \"Alert,10112,DISK_UTILIZATION>60.0\", \"Alert,10113,DISK_UTILIZATION>60.0\", \"Alert,10114,MEMORY_UTILIZATION>75.0\", \"Alert,10115,MEMORY_UTILIZATION>75.0,DISK_UTILIZATION>60.0\", \"No Alert,10116\", \"No Alert,10117\", \"Alert,10118,CPU_UTILIZATION>85.0\", \"No Alert,10119\", \"No Alert,10120\" ]";

        client
                .post(port, "localhost", "/alertapp/checkViolationCSV/")
                .sendMultipartForm(form, ar -> {
                    if (ar.succeeded()) {
                        testContext.assertTrue(ar.result().bodyAsString().equals(resultCompare));
                    } else {
                        System.out.println("Failed to upload");
                    }
                    async.complete();
                });

    }

}
