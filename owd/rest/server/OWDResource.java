package it.polito.ws_vs_gcm.owd.rest.server;

@Path("DelayEvaluation/{SN}/{PAYLOAD}")
public class OWDResource {

    @Context
    private UriInfo context;

    public OWDResource() {
    }

    @GET
    @Produces("application/json")
    public String getJson(@PathParam("SN") String i, @PathParam("PAYLOAD") String payload) throws JSONException {
        long time1 = System.currentTimeMillis();
        Runnable r = new MyThread(i, time1);
        new Thread(r).start();
        JSONObject js = new JSONObject();
        js.put("SN",i);
        return js.toString();
    }

    @PUT
    @Consumes("application/json")
    public void putJson(String content) {
    }

    public class MyThread implements Runnable {

        int SN;
        long time11;

        public MyThread(Object parameter1, Object parameter2) {
            this.SN = Integer.parseInt(parameter1.toString());
            this.time11 = Long.parseLong(parameter2.toString());
        }

        public void run() {
            SntpClient_ms client = new SntpClient_ms();
            if (client.requestTime("ntp1.inrim.it", 3000)) {
                long time2 = System.currentTimeMillis();
                long now = client.getNtpTime() + (System.currentTimeMillis() - client.getNtpTimeReference()) - (time2 - time11);
                System.out.println("SN= " + SN + " ,Time: " + now);
            } else {
                System.out.println("SN= " + SN + " ,Time: --------");
            }
        }
    }
}
