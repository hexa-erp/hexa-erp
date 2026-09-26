(function($) {
	'use strict';
	var $sidebar = $('.sidebar').first();
	if (!$sidebar.length)
		return;

	function setExpanded(button, expanded) {
		var $button = $(button);
		var $panel = $(document.getElementById($button.attr('aria-controls')));
		if (!$panel.length)
			return;
		$button.attr('aria-expanded', String(expanded));
		$panel.prop('hidden', !expanded);
	}

	$sidebar.on('click.hexaLayout', '[data-sidebar-toggle]', function() {
		setExpanded(this, $(this).attr('aria-expanded') !== 'true');
	});

	$sidebar
			.on(
					'keydown.hexaLayout',
					function(e) {
						var $target = $(e.target);
						var button = $target.closest('[data-sidebar-toggle]')[0];
						var $group = $target.closest('[data-sidebar-group]');
						if (!$group.length)
							return;
						var trigger = $group.find('[data-sidebar-toggle]')[0];
						if (e.key === 'ArrowLeft' || e.key === 'Escape') {
							e.preventDefault();
							setExpanded(trigger, false);
							trigger.focus();
						} else if (button && e.key === 'ArrowRight') {
							e.preventDefault();
							setExpanded(button, true);
							var firstLink = $(
									document.getElementById($(button).attr(
											'aria-controls'))).find('a')[0];
							if (firstLink)
								firstLink.focus();
						} else if (button
								&& (e.key === 'ArrowDown'
										|| e.key === 'ArrowUp'
										|| e.key === 'Home' || e.key === 'End')) {
							e.preventDefault();
							var $buttons = $sidebar
									.find('[data-sidebar-toggle]');
							var index = $buttons.index(button);
							if (e.key === 'Home')
								index = 0;
							else if (e.key === 'End')
								index = $buttons.length - 1;
							else
								index = (index
										+ (e.key === 'ArrowDown' ? 1 : -1) + $buttons.length)
										% $buttons.length;
							$buttons[index].focus();
						}
					});
}(jQuery));
