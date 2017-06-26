package it.polito.ws_vs_gcm.ndu.soap.server;

@WebService(serviceName = "TimePerformanceWS")
public class WS_NDU {

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