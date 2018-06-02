
function Scaler (innerViewport) {
	var innerViewport = innerViewport;

    this.perform = function() {

        var outerWidth = 
                parseInt(innerViewport.css('width')) + 
                parseInt(innerViewport.css('margin-left')) + parseInt(innerViewport.css('margin-right')) +
                (parseInt(innerViewport.css('border-width')) * 2) + 
                parseInt(innerViewport.css('padding-left')) + parseInt(innerViewport.css('padding-right'));


		var scale = window.innerWidth / outerWidth;
        
        scaleElement(innerViewport, scale);
        innerViewport.find('*').each(function() {
            scaleElement($(this), scale);
        });
    };

    function scaleElement(element, scale) {

        if (element.is('.skip-all') || element.closest('.skip-all').length) {
            return;
        }

        if (!element.is('.skip-height') && !element.closest('.skip-height').length) {
            scaleHeight(element, scale);
        }

        if (!element.is('.skip-width') && !element.closest('.skip-width').length) {
            scaleWidth(element, scale);
        }

        if (!element.is('.skip-margin') && !element.closest('.skip-margin').length) {
            scaleCss(element, "margin-top", scale);
            scaleCss(element, "margin-right", scale);
            scaleCss(element, "margin-bottom", scale);
            scaleCss(element, "margin-left", scale);
        }

        if (!element.is('.skip-padding') && !element.closest('.skip-padding').length) {
            scaleCss(element, "padding-top", scale);
            scaleCss(element, "padding-right", scale);
            scaleCss(element, "padding-bottom", scale);
            scaleCss(element, "padding-left", scale);
        }

        if (!element.is('.skip-font') && !element.closest('.skip-font').length) {
            scaleCss(element, "font-size", scale);
        }

        if (!element.is('.skip-absolut') && !element.closest('.skip-absolut').length) {
            scaleCss(element, "top", scale);
            scaleCss(element, "right", scale);
            scaleCss(element, "bottom", scale);
            scaleCss(element, "left", scale);
        }

        if (!element.is('.skip-line-height') && !element.closest('.skip-line-height').length) {
            scaleCss(element, "line-height", scale);
        }
    }

	function scaleCss(element, attr, scale) {
		element.css(attr, parseInt(element.css(attr))*scale);
	}

    function scaleWidth(element, scale) {
		element.width(element.width() * scale);
	}

	function scaleHeight(element, scale) {
		element.height(element.height() * scale);
	}
}


$(document).ready(function() {
	var scaler = new Scaler($('#inner-viewport'));
	scaler.perform();

	// $(window).resize(function () { 
	// 	scaler.perform(); 
	// });
});

