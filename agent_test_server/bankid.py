import qrcode
import qrcode.image.svg
from io import BytesIO
import base64

# Returns a base64-encoded SVG
def generate_bankid_qrcode(token):
    if not token:
        return None
    factory = qrcode.image.svg.SvgPathImage
    img = qrcode.make('bankid:///?autostarttoken=' + token + '&redirect=null', image_factory = factory)
    svg = BytesIO()
    img.save(svg)
    return base64.b64encode(svg.getvalue()).decode()
