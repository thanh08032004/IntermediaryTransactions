package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.*;
import hsf302.group3.intermediarytransactions.repository.IntermediaryInvoiceRepository;
import hsf302.group3.intermediarytransactions.repository.UserRepository;
import hsf302.group3.intermediarytransactions.repository.WalletRepository;
import hsf302.group3.intermediarytransactions.security.CustomUserDetails;
import hsf302.group3.intermediarytransactions.service.FeeService;
import hsf302.group3.intermediarytransactions.service.FileService;
import hsf302.group3.intermediarytransactions.service.IntermediaryInvoiceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/intermediary")
public class IntermediaryInvoiceController {

    private final IntermediaryInvoiceService invoiceService;
    private final IntermediaryInvoiceRepository invoiceRepository;
    private final FileService fileService;
    private final IntermediaryInvoiceService intermediaryService;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;


    // ================= LIST =================
    @GetMapping("/list")
    public String listInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Integer sellerId = userDetails.getUser().getId();
        Pageable pageable = PageRequest.of(page, size);

        Page<IntermediaryInvoice> invoicePage;

        if (keyword == null || keyword.isBlank()) {
            invoicePage = invoiceRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable);
        } else {
            invoicePage = invoiceRepository.findBySellerIdAndKeyword(sellerId, keyword, pageable);
        }

        model.addAttribute("invoices", invoicePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", invoicePage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "intermediary-list"; // template chỉ hiển thị đơn bán
    }

    @PostMapping("/create")
    public String createInvoice(
            @ModelAttribute IntermediaryInvoice invoice,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "mainIndex", required = false) Integer mainIndex,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 🔥 seller + status + token
        invoice.setSellerId(userDetails.getUser().getId());
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setShareToken(UUID.randomUUID().toString());

        // ✅ feeAmount + buyerTotal đã được JS tính sẵn từ form
        // invoice.getFeeAmount() và invoice.getBuyerTotal() đã đúng

        // 🔥 upload images
        List<IntermediaryInvoiceImage> images = new ArrayList<>();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                if (!file.isEmpty()) {
                    String url = fileService.upload(file);

                    IntermediaryInvoiceImage img = new IntermediaryInvoiceImage();
                    img.setImageUrl(url);
                    img.setIsMain(mainIndex != null && i == mainIndex);
                    img.setInvoice(invoice);

                    images.add(img);
                }
            }
        }
        invoice.setImages(images);

        // 🔥 save invoice
        invoiceRepository.save(invoice);

        return "redirect:/intermediary/view/" + invoice.getInvoiceId();
    }

    @GetMapping("/intermediary/{id}")
    public String detail(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        IntermediaryInvoice invoice = intermediaryService.getById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id " + id));

        model.addAttribute("invoice", invoice);

        // Lấy current user từ userDetails
        if (userDetails != null) {
            model.addAttribute("currentUser", userDetails.getUser());
        } else {
            model.addAttribute("currentUser", null);
        }

        model.addAttribute("editable", Boolean.TRUE);
        return "intermediary-detail";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("invoice", new IntermediaryInvoice());
        return "create-invoice";
    }
    // ================= JOIN (SHARE LINK) =================

    @PostMapping("/update/{id}")
    public String updateInvoice(
            @PathVariable String id,
            @ModelAttribute IntermediaryInvoice updatedInvoice,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "mainIndex", required = false) Integer mainIndex,
            @RequestParam(value = "deleteImages", required = false) List<Long> deleteImages,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        IntermediaryInvoice invoice = invoiceService.getById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // Kiểm tra quyền
        if (!invoice.getSellerId().equals(userDetails.getUser().getId())) {
            throw new AccessDeniedException("No permission");
        }

        if (invoice.getStatus() != InvoiceStatus.PENDING) {
            throw new RuntimeException("Only PENDING invoices can be updated");
        }

        invoice.setSubject(updatedInvoice.getSubject());
        invoice.setPrice(updatedInvoice.getPrice());
        invoice.setFeePayer(updatedInvoice.getFeePayer());
        invoice.setDescription(updatedInvoice.getDescription());
        invoice.setContactMethod(updatedInvoice.getContactMethod());
        invoice.setHiddenInfo(updatedInvoice.getHiddenInfo());

        BigDecimal price = invoice.getPrice() != null ? invoice.getPrice() : BigDecimal.ZERO;
        BigDecimal fee;
        BigDecimal oneMillion = new BigDecimal("1000000");
        BigDecimal fiveMillion = new BigDecimal("5000000");

        if (price.compareTo(oneMillion) <= 0) {
            fee = price.multiply(new BigDecimal("0.05"));
        } else if (price.compareTo(fiveMillion) <= 0) {
            fee = price.multiply(new BigDecimal("0.03"));
        } else {
            fee = price.multiply(new BigDecimal("0.02"));
        }
        BigDecimal buyerTotal;
        if (invoice.getFeePayer() == FeePayer.BUYER) {
            buyerTotal = price.add(fee);
        } else {
            buyerTotal = price.subtract(fee);
        }

        invoice.setFeeAmount(fee.setScale(0, RoundingMode.HALF_UP));
        invoice.setBuyerTotal(buyerTotal.setScale(0, RoundingMode.HALF_UP));

        // === Xử lý xóa ảnh ===
        if (deleteImages != null && !deleteImages.isEmpty() && invoice.getImages() != null) {
            invoice.getImages().removeIf(img -> deleteImages.contains(img.getId()));
        }

        // === Xử lý upload ảnh mới ===
        if (files != null && files.length > 0) {
            List<IntermediaryInvoiceImage> images = invoice.getImages() != null ? invoice.getImages() : new ArrayList<>();

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = fileService.upload(file);

                    IntermediaryInvoiceImage img = new IntermediaryInvoiceImage();
                    img.setImageUrl(url);
                    img.setIsMain(false);
                    img.setInvoice(invoice);

                    images.add(img);
                }
            }

            invoice.setImages(images);
        }

        // === Chọn main image ===
        if (mainIndex != null && invoice.getImages() != null
                && mainIndex >= 0 && mainIndex < invoice.getImages().size()) {
            for (int i = 0; i < invoice.getImages().size(); i++) {
                invoice.getImages().get(i).setIsMain(i == mainIndex);
            }
        } else if (invoice.getImages() != null && !invoice.getImages().isEmpty()) {
            // Nếu không chọn main, mặc định ảnh đầu tiên là main
            invoice.getImages().get(0).setIsMain(true);
        }

        invoiceService.save(invoice);
        redirectAttributes.addFlashAttribute("success", "Updated successfully!");
        return "redirect:/intermediary/view/" + id;
    }

    @GetMapping("/view/{id}")
    public String viewInvoice(@PathVariable("id") String id,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model) {
        IntermediaryInvoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        model.addAttribute("invoice", invoice);
        model.addAttribute("currentUser", userDetails.getUser()); // Thêm dòng này
        return "intermediary-detail";
    }

    // ================= DELETE =================
    @GetMapping("/delete/{id}")
    public String deleteInvoice(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        IntermediaryInvoice invoice = invoiceService.getById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (!invoice.getSellerId().equals(userDetails.getUser().getId())) {
            throw new AccessDeniedException("No permission");
        }

        if (invoice.getStatus() != InvoiceStatus.PENDING) {
            throw new RuntimeException("Only PENDING can delete");
        }

        invoiceService.delete(id);
        return "redirect:/intermediary/list";
    }

    @GetMapping("/join/{token}")
    public String joinInvoice(@PathVariable String token,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {

        IntermediaryInvoice invoice = invoiceRepository
                .findByShareToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid link"));

        if (userDetails == null) {
            // chưa login → redirect đến login với query param redirect
            redirectAttributes.addFlashAttribute("info", "Bạn cần đăng nhập để xem hóa đơn");
            return "redirect:/login?redirect=/intermediary/join/" + token;
        }

        // nếu đã login → gán buyer nếu muốn (có thể bỏ nếu ko muốn thay đổi buyer)
        if (invoice.getBuyerId() == null) {
            invoice.setBuyerId(userDetails.getUser().getId());
            invoiceRepository.save(invoice);
        }

        return "redirect:/intermediary/share/" + invoice.getInvoiceId();
    }
    @GetMapping("/bydetail/{id}")
    public String viewInvoiceDetail(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        IntermediaryInvoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        model.addAttribute("invoice", invoice);
        model.addAttribute("currentUser", userDetails != null ? userDetails.getUser() : null);

        return "intermediary-bydetail"; // chính là template bạn muốn hiển thị
    }
    @GetMapping("/share/{id}")
    public String shareInvoice(@PathVariable String id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        IntermediaryInvoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        model.addAttribute("invoice", invoice);
        model.addAttribute("isCustomer", true);

        if (userDetails != null) {
            model.addAttribute("currentUser", userDetails.getUser());
        } else {
            model.addAttribute("currentUser", null);
        }

        return "shareDetail";
    }

    @PostMapping("/pay/{id}")
    @Transactional
    public String payInvoice(@PathVariable String id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để thanh toán");
            return "redirect:/login";
        }

        IntermediaryInvoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (invoice.getStatus() != InvoiceStatus.PENDING) {
            redirectAttributes.addFlashAttribute("error", "Hóa đơn đã được xử lý");
            return "redirect:/intermediary/share/" + id;
        }

        Wallet buyerWallet = walletRepository.findByUserId(userDetails.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        BigDecimal amount = invoice.getBuyerTotal() != null ? invoice.getBuyerTotal() : BigDecimal.ZERO;

        if (buyerWallet.getBalance().compareTo(amount) < 0) {
            redirectAttributes.addFlashAttribute("error", "Tài khoản không đủ, vui lòng nạp thêm");
            return "redirect:/intermediary/share/" + id;
        }

        // Trừ tiền buyer
        buyerWallet.setBalance(buyerWallet.getBalance().subtract(amount));
        walletRepository.save(buyerWallet);

        // Update trạng thái chờ confirm
        invoice.setStatus(InvoiceStatus.CHECKING);
        invoice.setBuyerId(userDetails.getUser().getId());
        invoiceRepository.save(invoice);

        redirectAttributes.addFlashAttribute("success", "Thanh toán thành công, chờ xác nhận từ bạn!");
        return "redirect:/intermediary/share/" + id;
    }
    @PostMapping("/confirm/{id}")
    @Transactional
    public String confirmInvoice(@PathVariable String id,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {

        IntermediaryInvoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (invoice.getStatus() != InvoiceStatus.CHECKING) {
            redirectAttributes.addFlashAttribute("error", "Hóa đơn chưa thanh toán hoặc đã xác nhận");
            return "redirect:/intermediary/share/" + id;
        }

        // Cộng tiền seller
        Wallet sellerWallet = walletRepository.findByUserId(invoice.getSellerId())
                .orElseThrow(() -> new RuntimeException("Wallet not found for seller"));

        BigDecimal sellerReceive = invoice.getPrice() != null ? invoice.getPrice() : BigDecimal.ZERO;
        sellerWallet.setBalance(sellerWallet.getBalance().add(sellerReceive));
        walletRepository.save(sellerWallet);

        // Cập nhật trạng thái SUCCESS
        invoice.setStatus(InvoiceStatus.SUCCESS);
        invoiceRepository.save(invoice);

        redirectAttributes.addFlashAttribute("success", "Hóa đơn đã xác nhận và tiền đã chuyển cho người bán!");
        return "redirect:/intermediary/share/" + id;
    }
    @GetMapping("/my-buy")
    public String viewMyBuyInvoices(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User currentUser = userDetails.getUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<IntermediaryInvoice> invoicePage =
                invoiceRepository.findByBuyerIdOrderByCreatedAtDesc(currentUser.getId(), pageable);

        model.addAttribute("invoices", invoicePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", invoicePage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("currentUser", currentUser);

        return "intermediary-mybuy";
    }
}