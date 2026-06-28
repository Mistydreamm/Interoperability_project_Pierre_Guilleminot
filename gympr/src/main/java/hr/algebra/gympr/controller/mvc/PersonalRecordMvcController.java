package hr.algebra.gympr.controller.mvc;

import hr.algebra.gympr.dto.PersonalRecordDto;
import hr.algebra.gympr.entity.User;
import hr.algebra.gympr.enums.LiftType;
import hr.algebra.gympr.enums.MuscleGroup;
import hr.algebra.gympr.service.PersonalRecordService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/lifts")
public class PersonalRecordMvcController {

    private final PersonalRecordService recordService;

    public PersonalRecordMvcController(PersonalRecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping
    public String list(
        @RequestParam(required = false) LiftType lift,
        @RequestParam(required = false) MuscleGroup group,
        @RequestParam(required = false) Boolean milestone,
        @RequestParam(required = false) String query,
        Model model
    ) {
        boolean searching = lift != null || group != null || milestone != null || query != null;
        model.addAttribute("records", searching
            ? recordService.search(lift, group, milestone, query)
            : recordService.findAll());
        model.addAttribute("liftTypes", LiftType.values());
        model.addAttribute("muscleGroups", MuscleGroup.values());
        model.addAttribute("selectedLift", lift);
        model.addAttribute("selectedGroup", group);
        model.addAttribute("selectedMilestone", milestone);
        model.addAttribute("query", query);
        return "lifts/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("record", recordService.findById(id));
            return "lifts/detail";
        } catch (NoSuchElementException e) {
            return "redirect:/lifts";
        }
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newForm(Model model) {
        model.addAttribute("record", new PersonalRecordDto(
            null, null, null, BigDecimal.ZERO, 1, 1, null,
            null, "", "", null, false, "", null, null, null
        ));
        model.addAttribute("liftTypes", LiftType.values());
        model.addAttribute("muscleGroups", MuscleGroup.values());
        model.addAttribute("editMode", false);
        return "lifts/form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String create(
        @Valid @ModelAttribute("record") PersonalRecordDto dto,
        BindingResult result,
        @AuthenticationPrincipal User currentUser,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            model.addAttribute("liftTypes", LiftType.values());
            model.addAttribute("muscleGroups", MuscleGroup.values());
            model.addAttribute("editMode", false);
            return "lifts/form";
        }
        recordService.create(dto, currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "PR logged. Let's get it.");
        return "redirect:/lifts";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("record", recordService.findById(id));
            model.addAttribute("liftTypes", LiftType.values());
            model.addAttribute("muscleGroups", MuscleGroup.values());
            model.addAttribute("editMode", true);
            return "lifts/form";
        } catch (NoSuchElementException e) {
            return "redirect:/lifts";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(
        @PathVariable Long id,
        @Valid @ModelAttribute("record") PersonalRecordDto dto,
        BindingResult result,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            model.addAttribute("liftTypes", LiftType.values());
            model.addAttribute("muscleGroups", MuscleGroup.values());
            model.addAttribute("editMode", true);
            return "lifts/form";
        }
        recordService.update(id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "PR updated.");
        return "redirect:/lifts";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        recordService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "PR removed.");
        return "redirect:/lifts";
    }
}
