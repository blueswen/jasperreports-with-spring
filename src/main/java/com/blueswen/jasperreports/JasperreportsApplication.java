package com.blueswen.jasperreports;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

@SpringBootApplication
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class JasperreportsApplication {

    public static void main(String[] args) {
        SpringApplication.run(JasperreportsApplication.class, args);
    }

    @PostMapping("/report")
    @CrossOrigin(value = "*")
    public void report(HttpServletResponse response, @RequestBody Map<String, Object> payload) throws JRException, IOException {
        // target jasper filename e.g. sample.jasper
        String template = (String) payload.get("template");

        // check template file exists or not
        URL resource = this.getClass().getResource("/jasperreports/" + template);
        if (resource == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "template: "+ template + " does not exists.");
        }

        // output pdf filename
        String fileName = (String) payload.get("fileName");

        // using parameters in json as jasper report parameters from request
        Map<String, Object> params = (Map) payload.get("parameters");

        // using data in json as jasper report data source from request
        Collection<Map<String, ?>> rows = (Collection) payload.get("data");

        // dynamic loading different jasper report by request
        InputStream jasperStream = resource.openStream();
        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);

        // fill parameters and data source to jasper report
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JRMapCollectionDataSource(rows));

        response.setContentType("application/x-pdf");
        response.setHeader("Content-disposition", "inline; filename=" + fileName + ".pdf");

        final OutputStream outStream = response.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, outStream);
    }
}
