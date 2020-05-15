package com.doubtnut.assignment.controller;

import com.doubtnut.assignment.pdfgenerator.DataObject;
import com.doubtnut.assignment.pdfgenerator.HeaderFooter;
import com.doubtnut.assignment.pdfgenerator.PDFCreator;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//import redis.clients.jedis.Jedis;

/*
@author : Pradeep Kumar
@LastModified : 10-05-2020
@description : PDF generator for questions which is search by a user on the web/app but in below mentioned class few assumptions has taken care of those are :
1- This controller(end point) just taking care of PDF generation with its data
2- Here PDF data is set by method (getDataObjectList) manually for single user only, In real world we can get all users which are inactive in last 5 mins from db (mysql | Redis) where questions data is stored
3- Generated files are permanently saved on the  server in PDF_Reports folder and also downloadable in browser too
5- There are 3 options to manage 5 mins delay time
a) We can identify user inactivity on basis of user session and heartbeat api call send to the the system
c) We can write a script(cron) which get all users which are inactive since last 5 mins and generate PDF for all users  and send  that to queue for sending email and sms whatever is required .I used this #2 approach and written code for single user only
*/


@RestController
@RequestMapping(value = "api")
public class PdfGeneratorController {

    @Autowired
    ServletContext context;
    private static final String DIR = "/home/pradeep/Downloads/DoubtNut/PDF_Reports";
    private static final String TITLE = "DoubtNut_QuestionReport_";
    public static final String PDF_EXTENSION = ".pdf";

    @RequestMapping(value = "/pdf/download", method = RequestMethod.GET, produces = MediaType.APPLICATION_PDF_VALUE)
    public void downloadPDF(HttpServletRequest request, HttpServletResponse response)
            throws IOException{
        try {
            //Step 1-get users and user's questions who are inactive in last 5 mins and generate PDF for them
            // Assume there are multiple users but i am writing logic for a single user only and pass userId as static one
            List<Long> inactiveUsers = getInactiveUsers(); // get data from redis here just created array list
            Iterator itr=inactiveUsers.iterator();
            while(itr.hasNext()) {
                Long userId = (Long) itr.next();
                String pdfName = DIR + "/" + TITLE + userId + "_" + System.currentTimeMillis() + PDF_EXTENSION;
                pdfGenerator(pdfName, userId); // Genrate the PDF in the PDF_Reports(On server)
                // Now download the file in browser starts here
                response.setContentType("application/pdf");
                response.setHeader("Content-disposition", "attachment;filename=" + TITLE + userId + "_" + System.currentTimeMillis() + PDF_EXTENSION);
                File f = new File(pdfName);
                FileInputStream fis = new FileInputStream(f);
                DataOutputStream os = new DataOutputStream(response.getOutputStream());
                response.setHeader("Content-Length", String.valueOf(f.length()));
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = fis.read(buffer)) >= 0) {
                    os.write(buffer, 0, len);
                }
                // Now download the file in browser ends here

                // send data to Queue to send this PDF in email or SMS starts here
                sendDatatoQueue(pdfName, userId, buffer);
                // Send data to Queue to send this PDF in email or SMS ends here
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
   @author :Pradeep Kumar
   @last Modified : 10-05-2020
   @description : get Inactive Users only
   @params : None
   @return :datalist object(JSON)
   */
    private List<Long> getInactiveUsers() {
        try {
            //Jedis jedis = new Jedis(REDIS_HOST);
            // Get the stored data and print it
            ArrayList<Long> users = new ArrayList<>(); //Creating arraylist
            //users = jedis.(LRANGE myusersList 0 -1);
            users.add(11L);//Adding object in arraylist
        return users;
        } catch (Exception e){
            e.printStackTrace();
        }
    return null;
    }

    /*
    @author :Pradeep Kumar
    @last Modified : 10-05-2020
    @description : This function used for send data
    @params : pdfName(String)
    @return :No return Void type
    */
    private void sendDatatoQueue(String pdfName, Long userId, byte[] data) {
        try{
            // do something here
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /*
    @author :Pradeep Kumar
    @last Modified : 10-05-2020
    @description : This function  accept data object and genrate PDF
    @params : pdfName(String)
    @return :No return Void type
    */
    private void pdfGenerator(String pdfName,Long userId) {
        List<DataObject> dataObjList = getSimilarQuestions(userId);
        Document document = null;
        try {
            //Document is not auto-closable hence need to close it separately
            document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(
                    new File(pdfName)));
            HeaderFooter event = new HeaderFooter();
            event.setHeader("DoubtNut Question Report");
            writer.setPageEvent(event);
            document.open();
            String finaltitle = TITLE+userId;
            PDFCreator.addMetaData(document, finaltitle); // set meta Title
            PDFCreator.addTitlePage(document, finaltitle); // set Page Title
            PDFCreator.addContent(  document, dataObjList); // set PDF content here
        }catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("FileNotFoundException occurs.." + e.getMessage());
        }finally{
            if(null != document){
                document.close();
            }
        }
    }


    /*
    @author :Pradeep Kumar
    @last Modified : 10-05-2020
    @description : get User data from redis list but here i just set data manually to test the report
    @params : Long userId
    @return :datalist object(JSON)
    */
    private static List<DataObject> getSimilarQuestions(Long userId ){
        // get this data from redis here in case mysql table too
        // here just passed to test the PDF functionality
        List<DataObject> dataObjList = new ArrayList<DataObject>();
        DataObject d1 = new DataObject();
        d1.setQuestion("What is difference between ArrayList and LinkedList");
        dataObjList.add(d1);
        DataObject d2 = new DataObject();
        d2.setQuestion("What is hash map ?");
        dataObjList.add(d2);
        DataObject d3 = new DataObject();
        d3.setQuestion("What is java ?");
        dataObjList.add(d3);
        return dataObjList;
    }
}
