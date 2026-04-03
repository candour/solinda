from PIL import Image
import os

def optimize_jpg(path, quality=85):
    with Image.open(path) as img:
        img.save(path, "JPEG", quality=quality, optimize=True)
        print(f"Optimized {path} with quality {quality}")

drawable_dir = "app/src/main/res/drawable"
for filename in ["back.jpg", "markup_1000029005.jpg"]:
    path = os.path.join(drawable_dir, filename)
    if os.path.exists(path):
        optimize_jpg(path, quality=75)
