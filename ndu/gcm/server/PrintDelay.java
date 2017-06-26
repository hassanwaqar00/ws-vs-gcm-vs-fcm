package it.polito.ws_vs_gcm.ndu.gcm.server;

public class PrintDelay{
    public boolean Print(String path,String ID,long now) throws IOException{
        try {
          writer = new FileWriter(path, true);
        }catch (IOException e1) {
            e1.printStackTrace();
        }
        out = new PrintWriter(writer);
        out.write("SN= " + ID + " ,Time: " + now + " ms\n");
        out.close();
        writer.close();
        return true;
    }

}
