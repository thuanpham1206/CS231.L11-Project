import numpy as np 
import cv2
import base64
import io
from PIL import Image, ImageFilter, ImageEnhance

from PIL.ImageFilter import (
   BLUR, CONTOUR, DETAIL, EDGE_ENHANCE, EDGE_ENHANCE_MORE,
   EMBOSS, FIND_EDGES, SMOOTH, SMOOTH_MORE, SHARPEN
)

def read_image(data):
    decoded_data = base64.b64decode(data)
    np_data = np.fromstring(decoded_data, np.uint8)
    img = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)
    img_RGB = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    pil_im = Image.fromarray(img_RGB)
    return pil_im

def show_image(pil_im):
    buff = io.BytesIO()
    pil_im.save(buff, format="PNG")
    img_str = base64.b64encode(buff.getvalue())
    return ""+str(img_str, 'utf-8')

def sharpen_image(img, factor):
    return ImageEnhance.Sharpness(img).enhance(factor)

def main(data):
    img = read_image(data)
    output = img.filter(DETAIL)
    return show_image(output)