# -*- coding: utf-8 -*-
from PIL import Image, ImageDraw
import math

# Colors
DOUGH = (255, 220, 170)
DOUGH_DARK = (230, 190, 140)
CRUST = (200, 140, 80)
TOMATO = (220, 60, 60)
CHEESE = (255, 230, 150)
CHEESE_DARK = (240, 200, 100)
PEPPERONI = (180, 50, 50)
OLIVE = (50, 50, 50)
GREEN = (100, 180, 100)
BEIGE = (255, 245, 238)
SHADOW = (200, 200, 200)

num_frames = 24
frames = []

for i in range(num_frames):
    frame = Image.new('RGB', (128, 128), BEIGE)
    draw = ImageDraw.Draw(frame)

    t = i / (num_frames - 1)

    # Bounce effect
    bounce = abs(math.sin(t * math.pi * 2)) * 8
    y_offset = int(bounce)

    center_x = 64
    center_y = 60 + y_offset

    # Shadow
    draw.ellipse([center_x - 40, center_y + 42, center_x + 40, center_y + 52], fill=SHADOW)

    # Pizza base (ellipse for 3D effect)
    draw.ellipse([center_x - 45, center_y - 30, center_x + 45, center_y + 35], fill=DOUGH, outline=CRUST, width=4)

    # Crust edge
    draw.ellipse([center_x - 45, center_y - 30, center_x + 45, center_y + 35], fill=None, outline=CRUST, width=6)

    # Tomato sauce
    draw.ellipse([center_x - 38, center_y - 25, center_x + 38, center_y + 28], fill=TOMATO)

    # Cheese (scattered)
    cheese_positions = [
        (center_x - 25, center_y - 10), (center_x + 15, center_y - 15),
        (center_x - 10, center_y + 5), (center_x + 25, center_y + 8),
        (center_x, center_y - 20), (center_x - 30, center_y + 15),
    ]
    for cx, cy in cheese_positions:
        draw.ellipse([cx - 12, cy - 6, cx + 12, cy + 6], fill=CHEESE)

    # Pepperoni (circ pepper)
    pepperoni_positions = [
        (center_x - 15, center_y - 12), (center_x + 18, center_y - 8),
        (center_x + 5, center_y + 2), (center_x - 22, center_y + 10),
        (center_x + 28, center_y + 15), (center_x, center_y - 22),
    ]
    for px, py in pepperoni_positions:
        draw.ellipse([px - 8, py - 8, px + 8, py + 8], fill=PEPPERONI, outline=(150, 40, 40), width=1)

    # Green peppers (small rectangles)
    green_positions = [
        (center_x - 5, center_y - 8), (center_x + 12, center_y + 12),
    ]
    for gx, gy in green_positions:
        draw.ellipse([gx - 4, gy - 4, gx + 4, gy + 4], fill=GREEN)

    # Olives (black circles)
    olive_positions = [
        (center_x + 8, center_y - 18), (center_x - 20, center_y),
        (center_x + 30, center_y - 2), (center_x - 10, center_y + 18),
    ]
    for ox, oy in olive_positions:
        draw.ellipse([ox - 3, oy - 3, ox + 3, oy + 3], fill=OLIVE)

    # Steam/sizzle
    steam_offset = int(math.sin(t * math.pi * 2) * 2)

    frames.append(frame)

# Save as GIF
frames[0].save(
    'd:/suyeeNN/food-miniapp/images/logo.gif',
    save_all=True,
    append_images=frames[1:],
    duration=80,
    loop=0,
    optimize=True
)
print("Pizza GIF created!")
