package hexa.erp.shipinstruction.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import hexa.erp.common.controller.LookupResponses;

@Controller
@RequestMapping("/lookup")
public class ShipInstructionLookupController {
	@GetMapping("/sources/shipping-instruction")
	@ResponseBody
	public Map<String, Object> shipInstructionSources() {
		return LookupResponses.emptyResult();
	}
}
