package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.Role;
import hsf302.group3.intermediarytransactions.repository.PermissionRepository;
import hsf302.group3.intermediarytransactions.repository.RoleRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;

@Controller
@RequestMapping("/admin/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleController(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @GetMapping
    public String listRoles(Model model) {
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/role-list";
    }

    @GetMapping("/edit/{id}")
    public String editRolePermissions(@PathVariable Integer id, Model model) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        model.addAttribute("role", role);
        model.addAttribute("allPermissions", permissionRepository.findAll());
        return "admin/role-edit";
    }

    @PostMapping("/update")
    public String updatePermissions(@RequestParam Integer roleId,
                                    @RequestParam(required = false) List<Integer> permissionIds) {
        Role role = roleRepository.findById(roleId).orElseThrow();

        if (permissionIds != null) {
            role.setPermissions(new HashSet<>(permissionRepository.findAllById(permissionIds)));
        } else {
            role.setPermissions(new HashSet<>()); //go bo toan bo quyen
        }

        roleRepository.save(role);
        return "redirect:/admin/roles?success";
    }
}