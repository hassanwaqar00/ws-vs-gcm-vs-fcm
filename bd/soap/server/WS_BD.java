package it.polito.ws_vs_gcm.bd.soap.server;

@WebService(serviceName = "TimePerformanceWS")
public class WS_BD {

    public String battery_string = "";

    @WebMethod(operationName = "delay")
    public int delay(@WebParam(name = "SN") int SN, @WebParam(name = "PAYLOAD") String txt) throws IOException {
        long time1 = System.currentTimeMillis();
        Runnable r = new MyThread(SN, time1);
        new Thread(r).start();
        return SN;
    }


    @WebMethod(operationName = "rtt")
	public String rtt(@WebParam(name = "SN") int SN, @WebParam(name = "PAYLOAD") String txt) throws IOException {
        if (battery_string.equals(txt) == false) {
            battery_string = txt.toString();
            String path = "/battery_consumption_WS.txt";
            File file = new File(path);
            try {
                if (file.exists()) {
                    System.out.println("The file " + path + " exists.");
                } else if (file.createNewFile()) {
                    System.out.println("The file " + path + " has been created.");
                } else {
                    System.out.println("The file " + path + " cannot be created.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("File not created");
            }
            try {
                FileWriter fw = new FileWriter(file, true);
                fw.write(battery_string);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("SN= " + SN + " ,payload length:" + txt.length());
        return txt;
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
