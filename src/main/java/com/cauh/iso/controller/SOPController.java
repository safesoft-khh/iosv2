package com.cauh.iso.controller;

import com.cauh.common.entity.Account;
import com.cauh.common.entity.constant.UserType;
import com.cauh.common.security.annotation.CurrentUser;
import com.cauh.iso.component.DocumentViewer;
import com.cauh.iso.domain.Category;
import com.cauh.iso.domain.DocumentAccessLog;
import com.cauh.iso.domain.DocumentVersion;
import com.cauh.iso.domain.constant.DocumentAccessType;
import com.cauh.iso.domain.constant.DocumentStatus;
import com.cauh.iso.repository.DocumentVersionRepository;
import com.cauh.iso.service.CategoryService;
import com.cauh.iso.service.DocumentAccessLogService;
import com.cauh.iso.service.DocumentVersionService;
import com.cauh.iso.service.FileStorageService;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Controller
@RequiredArgsConstructor
@Slf4j
@SessionAttributes({"categoryList"})
public class SOPController {
    private final CategoryService categoryService;

    private final DocumentViewer documentViewer;

    private final DocumentVersionService documentVersionService;

    private final FileStorageService fileStorageService;

    private final DocumentAccessLogService documentAccessLogService;
    private final DocumentVersionRepository documentVersionRepository;

    @Value("${form.name}")
    private String formName;

    public static <T> Predicate<T> distinctByKey(
            Function<? super T, ?> keyExtractor) {

        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    //    @IsAllowedSOP
    @Transactional(readOnly = true)
    @GetMapping({
            "/sop/{status}",
            "/sop/{status}/{categoryId}",
            "/sop/{status}/{categoryId}/{sopId}"
    })
    public String sopList(@PathVariable("status") String stringStatus, @PathVariable(value = "categoryId", required = false) String categoryId, @PathVariable(value = "sopId", required = false) String sopId,
                          @CurrentUser Account user, Model model) {
        DocumentStatus status = DocumentStatus.valueOf(stringStatus.toUpperCase());

        BooleanBuilder builder = documentVersionService.getMainSOPPredicate(status, categoryId, sopId, null);
        log.info("@SOP 조회 조건 : {}", builder);
        Iterable<DocumentVersion> iterable = documentVersionService.findAll(builder);
        //Iterable<DocumentVersion> iterable = documentVersionRepository.findAll();
        List<DocumentVersion> sopList = StreamSupport.stream(iterable.spliterator(), false)
                .collect(toList());

        log.info("SOP List : {}", sopList);

        if(StringUtils.isEmpty(sopId)) {
            List<DocumentVersion> rfSopLists = documentVersionRepository.getSOPFoldersByStatus(status, categoryId);

            if (!ObjectUtils.isEmpty(rfSopLists)) {
                sopList.addAll(rfSopLists);
            }
        }

        List<DocumentVersion> filteredList = sopList.stream()
                .filter(distinctByKey(v -> v.getDocument().getId()))
                .sorted(Comparator.comparing(d -> d.getDocument().getDocId()))
                .collect(toList());

        if(!ObjectUtils.isEmpty(filteredList) && StringUtils.isEmpty(categoryId)) {
            model.addAttribute("categoryList", filteredList.stream()
                    .map(v -> v.getDocument().getCategory())
                    .distinct()
                    .sorted(Comparator.comparing((Category::getShortName)))
                    .collect(toList()));
        }
//        }
//            /**
//             * 외부 사용자
//             */
//            log.debug("@sopList : {}", sopList);
//            if(StringUtils.isEmpty(categoryId)) {
        if(user.getUserType() != UserType.AUDITOR) {
            log.info("User : {}", sopId);
            if (!StringUtils.isEmpty(sopId)) {
                BooleanBuilder rfBuilder = documentVersionService.getMainRFPredicate(status, Arrays.asList(sopId));
                log.debug("@RF 조회 조건 : {}", rfBuilder);
                Iterable<DocumentVersion> rfList = documentVersionService.findRFBySopId(rfBuilder);
                log.debug("RF List : {}", rfList);
                model.addAttribute("rfList", rfList);
            }
        } else if(!ObjectUtils.isEmpty(sopList)) {
            log.info("User2");
            List<String> sopIdList = StreamSupport.stream(sopList.spliterator(), false)
                    .map(s -> s.getDocument().getId())
                    .collect(toList());
            BooleanBuilder rfBuilder = documentVersionService.getMainRFPredicate(status, sopIdList);
            log.debug("@RF 조회 조건 : {}", rfBuilder);
            Iterable<DocumentVersion> rfList = documentVersionService.findRFBySopId(rfBuilder);
            model.addAttribute("rfList", rfList);
        }
//            sopList = documentVersionService.findAll()

//        }

        model.addAttribute("categoryId", categoryId);

        if (!StringUtils.isEmpty(categoryId)) {
            model.addAttribute("category", categoryService.findById(categoryId));
        }

        model.addAttribute("sopId", sopId);
        model.addAttribute("status", status);
        model.addAttribute("sopList", filteredList);

        model.addAttribute("formName", formName);
        return "sop/list";
    }

    @GetMapping("/sop/{status}/download/{docVerId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("docVerId") String id, HttpServletRequest request) {
        DocumentVersion documentVersion = documentVersionService.findById(id);
        documentAccessLogService.save(documentVersion, DocumentAccessType.DOWNLOAD);
        Resource resource = fileStorageService.loadFileAsResource(documentVersion.getFileName());
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("Could not determine file type.");
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + documentVersion.getOriginalFileName() + "\"")
                .body(resource);
    }
    
    @GetMapping("/rf/{status}/download/{docVerId}")
    public ResponseEntity<Resource> downloadRfFile(@PathVariable("docVerId") String id, @RequestParam("lang") String lang, HttpServletRequest request) {
        DocumentVersion documentVersion = documentVersionService.findById(id);
        documentAccessLogService.save(documentVersion, DocumentAccessType.DOWNLOAD);
        Resource resource;
        String orgFileName;
        if("kor".equals(lang)) {
            resource = fileStorageService.loadFileAsResource(documentVersion.getRfKorFileName());
            orgFileName = documentVersion.getRfKorOriginalFileName();
        } else if("eng".equals(lang)) {
            resource = fileStorageService.loadFileAsResource(documentVersion.getRfEngFileName());
            orgFileName = documentVersion.getRfEngOriginalFileName();
        } else {
            throw new RuntimeException("지원하지 않은 언어[" + lang + "] 입니다.");
        }
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("Could not determine file type.");
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + orgFileName+ "\"")
                .body(resource);
    }

    @GetMapping("/rf/view/{docVerId}")
    @ResponseBody
    public void viewer(@PathVariable("docVerId") String id, @RequestParam("lang") String lang, HttpServletResponse response) throws Exception {
        // Load file as Resource
        DocumentVersion documentVersion = documentVersionService.findById(id);

        documentAccessLogService.save(documentVersion, DocumentAccessType.VIEWER);


        if("kor".equals(lang)) {
            Resource resource = null;

            //.hwp일 때, 같이 업로드 한 PDF 파일로 뷰어 열기.
            if(documentVersion.getRfKorFileName().endsWith(".hwp")) {
                if(!ObjectUtils.isEmpty(documentVersion.getRfKorHwpPdfFileName())) {
                    resource = fileStorageService.loadFileAsResource(documentVersion.getRfKorHwpPdfFileName());
                    documentViewer.toHTML("pdf", resource.getInputStream(), response.getOutputStream());
                }
            } else {
                resource = fileStorageService.loadFileAsResource(documentVersion.getRfKorFileName());
                documentViewer.toHTML(documentVersion.getRfKorExt(), resource.getInputStream(), response.getOutputStream());
            }
        } else if("eng".equals(lang)) {
            Resource resource = null;

            //.hwp일 때, 같이 업로드 한 PDF 파일로 뷰어 열기.
            if(documentVersion.getRfEngFileName().endsWith(".hwp")) {
                if(!ObjectUtils.isEmpty(documentVersion.getRfEngHwpPdfFileName())) {
                    resource = fileStorageService.loadFileAsResource(documentVersion.getRfEngHwpPdfFileName());
                    documentViewer.toHTML("pdf", resource.getInputStream(), response.getOutputStream());
                }
            } else {
                resource = fileStorageService.loadFileAsResource(documentVersion.getRfEngFileName());
                documentViewer.toHTML(documentVersion.getRfEngExt(), resource.getInputStream(), response.getOutputStream());
            }
        } else {
            throw new RuntimeException("지원하지 않는 언어["+lang+"] 파일 입니다.");
        }
    }

    @GetMapping("/sop/{status}/viewer/{docVerId}")
    public void viewer(@PathVariable("status") String stringStatus, @PathVariable("docVerId") String docVerId, @RequestParam("page") int page,
                       @CurrentUser Account user,
                       HttpServletRequest request, HttpServletResponse response) throws Exception {
        String fileName = docVerId + "-" + page + ".jpg";
        DocumentStatus status = DocumentStatus.valueOf(stringStatus.toUpperCase());

        DocumentAccessType accessType = DocumentStatus.CONFIDENTIAL == status ? DocumentAccessType.TRAINING : DocumentAccessType.VIEWER;
        Optional<DocumentAccessLog> accessLog = documentAccessLogService.save(docVerId, accessType);

        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = "image/jpeg";
//        try {
//            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
//        } catch (IOException ex) {
//            log.info("Could not determine file type.");
//        }

        // Fallback to the default content type if type could not be determined
//        if (contentType == null) {
//            contentType = "application/octet-stream";
//        }

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName+ "\"");
        response.setContentType(contentType);


//            addTextWatermark(stringStatus.toUpperCase(), uuid, resource.getInputStream(), response.getOutputStream());
        watermark(status.name(), accessLog.isPresent() ? accessLog.get().getId() : "", resource.getInputStream(), response.getOutputStream());



//            return ResponseEntity.ok()
//                    .contentType(MediaType.parseMediaType(contentType))
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"noname.jpg\"")
//                    .body(resource);
    }

    public static void watermark(String watermarkText, String accessLogId, InputStream inputStream, OutputStream os) {
        try {
            BufferedImage original = ImageIO.read(inputStream);
            // 그래픽 컨텍스트 생성 및 앨리어스 방지 실행
            Graphics2D g2d = original.createGraphics();
            g2d.scale(1, 1);
            g2d.addRenderingHints(
                    new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON));
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 렌더링을 위한 워터 마크 텍스트 모양 생성
            //Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 200);
            // (해당 PC에 존재하는 폰트 이름,     스타일 ex)bold,    font 크기 이미지 해상도에따라 비율로 설정해야됨)
            Font font = new Font("Arial", Font.BOLD, 120);
            GlyphVector fontGV = font.createGlyphVector(g2d.getFontRenderContext(), watermarkText);
            Rectangle size = fontGV.getPixelBounds(g2d.getFontRenderContext(), 0, 0);
            Shape textShape = fontGV.getOutline();
            double textWidth = size.getWidth();
            double textHeight = size.getHeight();
            AffineTransform rotate45 = AffineTransform.getRotateInstance(Math.PI / -4d);
            Shape rotatedText = rotate45.createTransformedShape(textShape);
            AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f);
            g2d.setComposite(alphaComposite);
            // 4번 반복되는 경사를 사용하다
            g2d.setPaint(new GradientPaint(0, 0,
                    new Color(51, 102, 255),
                    original.getWidth() / 2, original.getHeight() / 2,
                    new Color(51, 102, 255)));
            g2d.setStroke(new BasicStroke(0.5f));

            // 피타 고라스+5픽셀 패딩을 사용하여 y방향으로 스텝을 조정합니다.
            // 높이 간격
            double yStep = Math.sqrt(textWidth * textWidth / 2) + 200;

            // 영상 렌더링 이상 텍스트
            int xx = 5;
            for (double x = -textHeight * xx; x < original.getWidth(); x += (textHeight * xx)) {
                double y = -yStep;
                for (; y < original.getHeight(); y += yStep) {
                    g2d.draw(rotatedText);
                    g2d.fill(rotatedText);
                    g2d.translate(0, yStep);
                }
                g2d.translate(textHeight * xx, -(y + yStep));
            }
            ImageIO.write(original, "jpg", os);
        } catch (Exception error) {
            log.error("Error : ", error);
        } finally {
            log.debug("SOP Viewer 이미지 생성 완료!");
            try {
                if (os != null) {
                    os.flush();
                    os.close();
                }
            } catch (IOException ioe) {}
        }
    }

    protected void addTextWatermark(String watermarkText, String docVerId, InputStream inputStream, OutputStream os) {
        try {
            BufferedImage sourceImage = ImageIO.read(inputStream);
            Graphics2D g2d = (Graphics2D) sourceImage.getGraphics();

            // initializes necessary graphic properties

            g2d.setColor(Color.lightGray);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            g2d.drawString(docVerId, 30, 50);


            AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
            g2d.setComposite(alphaChannel);
            g2d.setColor(new Color(51, 102, 255));
            AffineTransform affineTransform = AffineTransform.getRotateInstance(Math.PI / 4.0);
//            affineTransform.rotate(Math.toRadians(45), 0, 0);
            Font font = new Font("Arial", Font.BOLD, 120);
            font.deriveFont(affineTransform);

//            g2d.setTransform(affineTransform);
            g2d.setFont(font);
            FontMetrics fontMetrics = g2d.getFontMetrics();
            Rectangle2D rect = fontMetrics.getStringBounds(watermarkText, g2d);

            // calculates the coordinate where the String is painted
            int centerX = (sourceImage.getWidth() - (int) rect.getWidth()) / 2;
            int centerY = sourceImage.getHeight() / 2;

            // paints the textual watermark
            g2d.drawString(watermarkText, centerX, centerY);


            ImageIOUtil.writeImage(sourceImage, "png", os);
//            ImageIO.createImageOutputStream(os);
//            ImageIO.write(sourceImage, "png", destImageFile);
            g2d.dispose();

            System.out.println("The tex watermark is added to the image.");
            os.flush();
            os.close();

        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

//    @GetMapping("/rd/viewer/{docVerId}")
//    public void rdViewer(@PathVariable("docVerId") String id, HttpServletResponse response) throws Exception {
//        response.setContentType("application/octet-stream");
//        // Load file as Resource
//        try (OutputStream os = response.getOutputStream()) {
//            DocumentVersion documentVersion = documentVersionService.findById(id);
//            Resource resource = fileStorageService.loadFileAsResource(documentVersion.getFileName());
//
//            IXDocReport report = XDocReportRegistry.getRegistry().loadReport(resource.getInputStream(), TemplateEngineKind.Velocity);
//            IContext context = report.createContext();
//
//
//            Options options = Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.XWPF);
//            report.convert(context, options, os);
//        }
//    }
//    @GetMapping("/rd/{docVerId}")
//    public void rdViewer(@PathVariable("docVerId") String id, HttpServletResponse response) throws Exception {
//        // Load file as Resource
//        try (OutputStream os = response.getOutputStream()) {
//            DocumentVersion documentVersion = documentVersionService.findById(id);
//            Resource resource = fileStorageService.loadFileAsResource(documentVersion.getFileName());
//
//            XWPFDocument document = new XWPFDocument(resource.getInputStream());
//
//            XHTMLOptions options = XHTMLOptions.create();
//            XHTMLConverter.getInstance().convert(document, os, options);
//        }
//    }
}
