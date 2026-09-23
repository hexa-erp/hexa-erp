package hexa.erp.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class HomeController {
	@GetMapping("/")
	public RedirectView home() {
		RedirectView redirect = new RedirectView("/master/partner", true);
		redirect.setExposeModelAttributes(false);
		return redirect;
	}
}
