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
import java.util.Date;
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

            row.createCell(0).setCellValue(activity.getType() != null ? activity.getType() : "");
            row.createCell(1).setCellValue(activity.getDistance() != null ? activity.getDistance() : 0);
            row.createCell(2).setCellValue(activity.getAverageHeartRate() != null ? activity.getAverageHeartRate() : 0);
            row.createCell(3).setCellValue(activity.getMaxHeartRate() != null ? activity.getMaxHeartRate() : 0);
            row.createCell(4).setCellValue(activity.getStartDateLocal() != null ? activity.getStartDateLocal().toLocalDateTime().toString() : "");
            row.createCell(5).setCellValue(activity.getAverageWatts() != null ? activity.getAverageWatts() : 0);
            row.createCell(6).setCellValue((Date) (activity.getNormalizedPower() != null ? activity.getNormalizedPower() : ""));
            row.createCell(7).setCellValue(activity.getElapsedTime() != null ? activity.getElapsedTime() : 0);
            row.createCell(8).setCellValue(activity.getLaps() != null ? activity.getLaps() : "");
            row.createCell(9).setCellValue(
                    (activity.getDescription() != null ? activity.getDescription() : "") + " " +
                            (activity.getDescriptionTyped() != null ? activity.getDescriptionTyped() : "")
            );

            if ("Ride".equals(activity.getType())) {
                row.createCell(10).setCellValue(
                        stravaActivityService.calculateAverageSpeed(
                                activity.getDistance() != null ? activity.getDistance() : 0,
                                activity.getMoving_time() != null ? activity.getMoving_time() : 0
                        ) + "KM/H"
                );
            } else {
                row.createCell(10).setCellValue(
                        stravaActivityService.paceFromDistanceAndMovingTime(
                                activity.getDistance() != null ? activity.getDistance() : 0,
                                activity.getMoving_time() != null ? activity.getMoving_time() : 0
                        ) + "MIN/KM"
                );
            }

            row.createCell(11).setCellValue(activity.getTotalElevationGain() != null ? activity.getTotalElevationGain() : 0);
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


