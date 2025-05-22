package com.migrease.helper;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.migrease.model.Booking;
import com.migrease.model.FurnitureItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

public class BookingReceiptGenerator {

    private static final String SAVE_PATH = Paths.get("receipts").toAbsolutePath().toString() + File.separator;
    // Main theme colors to match the CSS theme
    private static final BaseColor PRIMARY_COLOR = new BaseColor(255, 81, 47); // #FF512F
    private static final BaseColor SECONDARY_COLOR = new BaseColor(221, 36, 118); // #DD2476
    private static final BaseColor LIGHT_BG = new BaseColor(247, 249, 252); // #f7f9fc
    private static final BaseColor BORDER_COLOR = new BaseColor(224, 224, 224); // #e0e0e0
    private static final BaseColor TEXT_COLOR = new BaseColor(51, 51, 51); // #333333

    public static void generateReceipt(Booking booking) {
        String fileName = SAVE_PATH + booking.getBookingId() + ".pdf";

        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        try {
            FileOutputStream out = new FileOutputStream(fileName);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // Create custom page event for header/footer
            writer.setPageEvent(new HeaderFooterPageEvent());

            // Add gradients and styling
            addReceiptContent(document, booking);

            document.close();
            out.close();

            System.out.println("📄 Enhanced PDF receipt generated at: " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addReceiptContent(Document document, Booking booking) throws DocumentException, IOException {
        // ---- HEADER SECTION WITH LOGO ----
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1f, 1f});

        // Logo and company name cell
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);

        // Add truck logo image
        Image truckLogo = getTruckLogo();
        if (truckLogo != null) {
            truckLogo.scaleToFit(80, 80);
            Paragraph logoPara = new Paragraph();
            logoPara.add(new Chunk(truckLogo, 0, 0, true));
            logoPara.add(new Chunk("  MigrEase", new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, PRIMARY_COLOR)));
            logoCell.addElement(logoPara);
        } else {
            // Fallback if image loading fails
            Paragraph companyName = new Paragraph("🚛 MigrEase", new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, PRIMARY_COLOR));
            logoCell.addElement(companyName);
        }

        // Booking receipt title cell
        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, SECONDARY_COLOR);
        Paragraph title = new Paragraph("BOOKING RECEIPT", titleFont);
        title.setAlignment(Element.ALIGN_RIGHT);
        titleCell.addElement(title);

        Font dateFont = new Font(Font.FontFamily.HELVETICA, 12);
        Paragraph dateInfo = new Paragraph("Date: " + booking.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")), dateFont);
        dateInfo.setAlignment(Element.ALIGN_RIGHT);
        titleCell.addElement(dateInfo);

        headerTable.addCell(logoCell);
        headerTable.addCell(titleCell);
        headerTable.setSpacingAfter(20f);
        document.add(headerTable);

        // ---- BOOKING INFO BOX ----
        PdfPTable bookingInfoTable = new PdfPTable(2);
        bookingInfoTable.setWidthPercentage(100);
        bookingInfoTable.setSpacingBefore(10f);
        bookingInfoTable.setSpacingAfter(20f);

        // BOOKING ID with special styling
        PdfPCell bookingIdHeaderCell = new PdfPCell();
        bookingIdHeaderCell.setBackgroundColor(PRIMARY_COLOR);
        bookingIdHeaderCell.setPadding(8f);
        bookingIdHeaderCell.setColspan(2);

        Font bookingIdFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.WHITE);
        Paragraph bookingIdPara = new Paragraph("Booking ID: " + booking.getBookingId(), bookingIdFont);
        bookingIdPara.setAlignment(Element.ALIGN_CENTER);
        bookingIdHeaderCell.addElement(bookingIdPara);
        bookingInfoTable.addCell(bookingIdHeaderCell);

        // Customer and Address Information
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, TEXT_COLOR);
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, TEXT_COLOR);

        // Left side: Customer details
        PdfPCell customerCell = new PdfPCell();
        customerCell.setBorder(Rectangle.BOX);
        customerCell.setBorderColor(BORDER_COLOR);
        customerCell.setPadding(10f);

        Paragraph customerTitle = new Paragraph("CUSTOMER DETAILS", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, SECONDARY_COLOR));
        customerTitle.setSpacingAfter(10f);
        customerCell.addElement(customerTitle);

        Paragraph customerName = new Paragraph("Name: " + booking.getUser().getName(), valueFont);
        customerName.setIndentationLeft(10f);
        customerName.setSpacingAfter(5f);
        customerCell.addElement(customerName);

        // Add more customer details if available like email, phone
        Paragraph customerContact = new Paragraph("Contact: " + (booking.getUser().getPhone() != null ? booking.getUser().getPhone() : "N/A"), valueFont);
        customerContact.setIndentationLeft(10f);
        customerContact.setSpacingAfter(5f);
        customerCell.addElement(customerContact);

        // Right side: Move details
        PdfPCell moveCell = new PdfPCell();
        moveCell.setBorder(Rectangle.BOX);
        moveCell.setBorderColor(BORDER_COLOR);
        moveCell.setPadding(10f);

        Paragraph moveTitle = new Paragraph("MOVE DETAILS", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, SECONDARY_COLOR));
        moveTitle.setSpacingAfter(10f);
        moveCell.addElement(moveTitle);

        Paragraph pickupAddress = new Paragraph("From: " + booking.getPickupAddress(), valueFont);
        pickupAddress.setIndentationLeft(10f);
        pickupAddress.setSpacingAfter(5f);
        moveCell.addElement(pickupAddress);

        Paragraph dropAddress = new Paragraph("To: " + booking.getDropAddress(), valueFont);
        dropAddress.setIndentationLeft(10f);
        dropAddress.setSpacingAfter(5f);
        moveCell.addElement(dropAddress);

        Paragraph distance = new Paragraph("Distance: " + String.format("%.2f", booking.getDistanceInKm()) + " km", valueFont);
        distance.setIndentationLeft(10f);
        moveCell.addElement(distance);

        bookingInfoTable.addCell(customerCell);
        bookingInfoTable.addCell(moveCell);
        document.add(bookingInfoTable);

        // ---- ITEMS TABLE TITLE ----
        Paragraph itemsTitle = new Paragraph("ITEMS DETAILS", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, PRIMARY_COLOR));
        itemsTitle.setSpacingBefore(20f);
        itemsTitle.setSpacingAfter(10f);
        document.add(itemsTitle);

        // ---- FURNITURE ITEMS TABLE ----
        PdfPTable itemsTable = new PdfPTable(4);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[]{0.5f, 2f, 1f, 1.5f});

        Font tableHeaderFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);

        // Create gradient for header
        GradientColor gradientColor = new GradientColor(PRIMARY_COLOR, SECONDARY_COLOR);

        addStyledHeaderCell(itemsTable, "S.No", tableHeaderFont, gradientColor);
        addStyledHeaderCell(itemsTable, "Item Category", tableHeaderFont, gradientColor);
        addStyledHeaderCell(itemsTable, "Quantity", tableHeaderFont, gradientColor);
        addStyledHeaderCell(itemsTable, "Weight (kg)", tableHeaderFont, gradientColor);

        BaseColor altRowColor = new BaseColor(245, 245, 245);
        boolean alt = false;
        int counter = 1;

        // Add items with alternating row colors
        for (FurnitureItem item : booking.getFurnitureItems()) {
            BaseColor rowColor = alt ? altRowColor : BaseColor.WHITE;

            itemsTable.addCell(getStyledCell(String.valueOf(counter++), rowColor, Element.ALIGN_CENTER));
            itemsTable.addCell(getStyledCell(item.getCategory(), rowColor, Element.ALIGN_LEFT));
            itemsTable.addCell(getStyledCell(item.getQuantity() + " pcs", rowColor, Element.ALIGN_CENTER));
            itemsTable.addCell(getStyledCell(String.format("%.2f", item.getWeight()) + " kg", rowColor, Element.ALIGN_RIGHT));

            alt = !alt;
        }

        document.add(itemsTable);

        // ---- PRICING SUMMARY ----
        Paragraph summaryTitle = new Paragraph("PAYMENT SUMMARY", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, PRIMARY_COLOR));
        summaryTitle.setSpacingBefore(25f);
        summaryTitle.setSpacingAfter(10f);
        document.add(summaryTitle);

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(60);
        summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summaryTable.setSpacingBefore(10f);

        // Add pricing details with proper rupee symbol
        Font summaryLabelFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, TEXT_COLOR);
        Font summaryValueFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, TEXT_COLOR);

        addSummaryRow(summaryTable, "Total Weight:", String.format("%.2f", booking.getTotalWeight()) + " kg", summaryLabelFont, summaryValueFont);
        addSummaryRow(summaryTable, "Transport Cost:", "₹" + String.format("%,.2f", booking.getTransportCost()), summaryLabelFont, summaryValueFont);
        addSummaryRow(summaryTable, "Packing Charges:", "₹" + String.format("%,.2f", booking.getPackingCost()), summaryLabelFont, summaryValueFont);

        // Add total row with special styling
        Font totalLabelFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, PRIMARY_COLOR);
        Font totalValueFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, SECONDARY_COLOR);

        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL AMOUNT:", totalLabelFont));
        totalLabelCell.setBorder(Rectangle.TOP);
        totalLabelCell.setPaddingTop(8f);
        totalLabelCell.setBorderColorTop(BORDER_COLOR);

        PdfPCell totalValueCell = new PdfPCell(new Phrase("₹" + String.format("%,.2f", booking.getTotalCost()), totalValueFont));
        totalValueCell.setBorder(Rectangle.TOP);
        totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValueCell.setPaddingTop(8f);
        totalValueCell.setBorderColorTop(BORDER_COLOR);

        summaryTable.addCell(totalLabelCell);
        summaryTable.addCell(totalValueCell);

        document.add(summaryTable);

        // ---- THANK YOU NOTE ----
        PdfPTable thankYouTable = new PdfPTable(1);
        thankYouTable.setWidthPercentage(100);
        thankYouTable.setSpacingBefore(40f);

        PdfPCell thankYouCell = new PdfPCell();
        thankYouCell.setBorder(Rectangle.NO_BORDER);
        thankYouCell.setPadding(15f);
        thankYouCell.setBackgroundColor(LIGHT_BG);

        Font thankYouFont = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC);
        Paragraph thankYou = new Paragraph("Thank you for choosing MigrEase for your relocation needs!\nWe are committed to making your move as smooth and stress-free as possible.", thankYouFont);
        thankYou.setAlignment(Element.ALIGN_CENTER);
        thankYouCell.addElement(thankYou);

        thankYouTable.addCell(thankYouCell);
        document.add(thankYouTable);
    }

    private static Image getTruckLogo() {
        try {
            // Try to load the truck logo (you should replace this with an actual image path or resource)
            // For demo, we're using an external resource - in production, use a local file
            URL imageUrl = new URL("https://cdn-icons-png.flaticon.com/512/3774/3774270.png");
            Image img = Image.getInstance(imageUrl);
            return img;
        } catch (Exception e) {
            System.out.println("Could not load truck logo: " + e.getMessage());
            return null;
        }
    }

    private static void addStyledHeaderCell(PdfPTable table, String text, Font font, GradientColor gradientColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(gradientColor.getStartColor());
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8f);
        table.addCell(cell);
    }

    private static PdfPCell getStyledCell(String text, BaseColor bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(8f);
        return cell;
    }

    private static void addSummaryRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(5f);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPaddingBottom(5f);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    // Custom PageEvent class for adding header and footer
    static class HeaderFooterPageEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();

            // Add page number at bottom
            Font footerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.GRAY);
            Phrase footer = new Phrase("Page " + writer.getPageNumber() + " | MigrEase - Making Relocations Stress-Free", footerFont);

            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    footer,
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 20, 0);

            // Add a colored line at bottom
            cb.setColorStroke(new BaseColor(255, 81, 47));
            cb.setLineWidth(1.5f);
            cb.moveTo(document.left(), document.bottom() - 10);
            cb.lineTo(document.right(), document.bottom() - 10);
            cb.stroke();
        }
    }

    // Helper class for gradient colors
    static class GradientColor {
        private BaseColor startColor;
        private BaseColor endColor;

        public GradientColor(BaseColor startColor, BaseColor endColor) {
            this.startColor = startColor;
            this.endColor = endColor;
        }

        public BaseColor getStartColor() {
            return startColor;
        }

        public BaseColor getEndColor() {
            return endColor;
        }
    }
}