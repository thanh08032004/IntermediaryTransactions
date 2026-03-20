package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.IntermediaryInvoice;
import hsf302.group3.intermediarytransactions.security.CustomUserDetails;
import hsf302.group3.intermediarytransactions.service.IntermediaryInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/intermediary")
public class IntermediaryInvoiceController {

    private final IntermediaryInvoiceService invoiceService;

    // ------------------- List invoices -------------------
    @GetMapping("/list")
    public String listInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        Integer userId = userDetails.getUser().getId();

        Page<IntermediaryInvoice> invoicePage = invoiceService.getUserInvoices(userId, page, size, keyword);

        model.addAttribute("invoices", invoicePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", invoicePage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);

        return "intermediary-list"; // Thymeleaf template
    }

    // ------------------- View invoice -------------------
    @GetMapping("/view/{id}")
    public String viewInvoice(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        IntermediaryInvoice invoice = invoiceService.getById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        Integer currentUserId = userDetails.getUser().getId();
        boolean isSellerOrBuyer = invoice.getSellerId().equals(currentUserId) || invoice.getBuyerId().equals(currentUserId);
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN"));

        if (!isSellerOrBuyer && !isAdmin) {
            throw new AccessDeniedException("You cannot view this invoice");
        }

        model.addAttribute("invoice", invoice);
        model.addAttribute("editable", invoice.getStatus().equals("PENDING") && invoice.getSellerId().equals(currentUserId));

        return "intermediary-detail";
    }

    // ------------------- Update invoice (only PENDING) -------------------
    @PostMapping("/update/{id}")
    public String updateInvoice(
            @PathVariable String id,
            @ModelAttribute IntermediaryInvoice updatedInvoice,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        IntermediaryInvoice invoice = invoiceService.getById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        Integer currentUserId = userDetails.getUser().getId();
        boolean isSeller = invoice.getSellerId().equals(currentUserId);

        if (!isSeller) {
            throw new AccessDeniedException("You cannot update this invoice");
        }

        if (!invoice.getStatus().equals("PENDING")) {
            throw new RuntimeException("Only PENDING invoices can be updated");
        }

        invoice.setInvoiceCode(updatedInvoice.getInvoiceCode());
        invoice.setSubject(updatedInvoice.getSubject());
        invoice.setPrice(updatedInvoice.getPrice());
        invoice.setFeePayer(updatedInvoice.getFeePayer());
        invoice.setFeeAmount(updatedInvoice.getFeeAmount());
        invoice.setBuyerTotal(updatedInvoice.getBuyerTotal());
        invoice.setContactMethod(updatedInvoice.getContactMethod());
        invoice.setShareLink(updatedInvoice.getShareLink());

        invoiceService.save(invoice);

        redirectAttributes.addFlashAttribute("success", "updated");
        return "redirect:/intermediary/view/" + id;
    }

    // ------------------- Delete invoice (only PENDING) -------------------
    @GetMapping("/delete/{id}")
    public String deleteInvoice(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        IntermediaryInvoice invoice = invoiceService.getById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        Integer currentUserId = userDetails.getUser().getId();
        boolean isSeller = invoice.getSellerId().equals(currentUserId);

        if (!isSeller) {
            throw new AccessDeniedException("You cannot delete this invoice");
        }

        if (!invoice.getStatus().equals("PENDING")) {
            throw new RuntimeException("Only PENDING invoices can be deleted");
        }

        invoiceService.delete(id);
        redirectAttributes.addFlashAttribute("success", "deleted");

        return "redirect:/intermediary/list";
    }
}