package com.example.training.services;

import com.example.training.model.Activity;
import com.example.training.model.ReadyToSendAi;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExportService {

    private StravaActivityService stravaActivityService;

    public ExportService() throws FileNotFoundException {
    }

    public HSSFWorkbook export(List<Activity> activities) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Aktywności");

        HSSFRow headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("TYP");
        headerRow.createCell(1).setCellValue("DYSTANS W METRACH");
        headerRow.createCell(2).setCellValue("ŚREDNIE TĘTNO");
        headerRow.createCell(3).setCellValue("MAX TĘTNO");
        headerRow.createCell(4).setCellValue("DATA");
        headerRow.createCell(5).setCellValue("ŚREDNIE WATY");
        headerRow.createCell(6).setCellValue("ZNORMALIZOWANE WATY");
        headerRow.createCell(7).setCellValue("CZAS TRWANIA W SEKUNDACH");
        headerRow.createCell(8).setCellValue("OKRĄŻENIA");
        headerRow.createCell(9).setCellValue("OPIS");
        headerRow.createCell(10).setCellValue("TEMPO");
        headerRow.createCell(11).setCellValue("PRZEWYŻSZENIA");

        int rowNum = 1;
        for (Activity activity : activities) {
            HSSFRow row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(activity.getType());
            row.createCell(1).setCellValue(activity.getDistance());
            row.createCell(2).setCellValue(activity.getAverageHeartRate());
            row.createCell(3).setCellValue(activity.getMaxHeartRate());
            row.createCell(4).setCellValue(activity.getStartDateLocal().toLocalDateTime());
            row.createCell(5).setCellValue(activity.getAverageWatts());
            if(activity.getNormalizedPower() != null ) {
                row.createCell(6).setCellValue(activity.getNormalizedPower());
            } else {
                row.createCell(6).setCellValue("");
            }
            row.createCell(7).setCellValue(activity.getElapsedTime());

            row.createCell(8).setCellValue(activity.getLaps());
            row.createCell(9).setCellValue(activity.getDescription() + ' ' + activity.getDescriptionTyped());
            switch (activity.getType()) {
                case "Ride":
                    row.createCell(10).setCellValue(stravaActivityService.calculateAverageSpeed(activity.getDistance(), activity.getMoving_time()) + "KM/H");
                    break;
                default:
                    row.createCell(10).setCellValue(stravaActivityService.paceFromDistanceAndMovingTime(activity.getDistance(), activity.getMoving_time()) + "MIN/KM");
            }
            row.createCell(11).setCellValue(activity.getTotalElevationGain());
        }

        // 🔹 Zapis do pliku
        try(FileOutputStream fileOut = new FileOutputStream("aktywności.xls"))
        {
            workbook.write(fileOut);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        workbook.close();
        return workbook;
    }




}


