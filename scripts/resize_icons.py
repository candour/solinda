from PIL import Image
import os

def resize_image(path, size):
    with Image.open(path) as img:
        img = img.resize((size, size), Image.Resampling.LANCZOS)
        img.save(path)
        print(f"Resized {path} to {size}x{size}")

res_dir = "app/src/main/res"
densities = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192
}

for density, size in densities.items():
    folder = os.path.join(res_dir, density)
    for filename in ["ic_launcher.png", "ic_launcher_round.png"]:
        path = os.path.join(folder, filename)
        if os.path.exists(path):
            resize_image(path, size)

# Also resize the drawable/app_icon_round.png to 512
app_icon_path = "app/src/main/res/drawable/app_icon_round.png"
if os.path.exists(app_icon_path):
    resize_image(app_icon_path, 512)
