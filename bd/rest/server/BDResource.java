package it.polito.ws_vs_gcm.bd.rest.server;

@Path("PowerConsumptionEvaluation/{SN}/{PAYLOAD}")
public class BDResource {
    @Context
    private UriInfo context;
    public String battery_string = "";
    public BDResource() {
    }

    @GET
    @Produces("application/json")
    public String getJson(@PathParam("SN") String i, @PathParam("PAYLOAD") String payload) throws JSONException {
        int iii=Integer.parseInt(i.toString());
        if (iii%300==0) {
            String path = "/home/developer/Desktop/Dario/battery_consumption_WSJSON.txt";
            File file = new File(path);
            try {
                if (file.exists()) {
                    System.out.println("Il file " + path + " esiste");
                } else if (file.createNewFile()) {
                    System.out.println("Il file " + path + " è¨ stato creato");
                } else {
                    System.out.println("Il file " + path + " non può essere creato");
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("File non creato");
            }
            try {
                FileWriter fw = new FileWriter(file, true);
                fw.write(payload);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JSONObject js = new JSONObject();
        js.put("SN",i);
        js.put("PAYLOAD",payload);
        return js.toString();
    }

    @PUT
    @Consumes("application/json")
    public void putJson(String content) {
    }
}
