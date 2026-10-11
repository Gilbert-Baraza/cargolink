import xml.etree.ElementTree as ET
import sys

try:
    tree = ET.parse(r'C:\Users\Engineer Jed\Downloads\icon.svg')
    root = tree.getroot()

    width = root.attrib.get('width', '2048').replace('px','')
    height = root.attrib.get('height', '2048').replace('px','')

    vd = ET.Element('vector', {
        'xmlns:android': 'http://schemas.android.com/apk/res/android',
        'android:width': f'{width}dp',
        'android:height': f'{height}dp',
        'android:viewportWidth': width,
        'android:viewportHeight': height
    })

    ns = {'svg': 'http://www.w3.org/2000/svg'}
    paths = root.findall('.//svg:path', ns)
    if not paths:
        paths = root.findall('.//path')

    for path in paths:
        d = path.attrib.get('d')
        fill = path.attrib.get('fill', '#000000')
        transform = path.attrib.get('transform')
        
        if transform and 'translate' in transform:
            inner = transform.replace('translate(', '').replace(')', '')
            parts = inner.split(',')
            tx = parts[0].strip()
            ty = parts[1].strip() if len(parts) > 1 else '0'
            group = ET.SubElement(vd, 'group', {
                'android:translateX': tx,
                'android:translateY': ty
            })
            ET.SubElement(group, 'path', {
                'android:fillColor': fill,
                'android:pathData': d
            })
        else:
            ET.SubElement(vd, 'path', {
                'android:fillColor': fill,
                'android:pathData': d
            })

    xml_str = ET.tostring(vd, encoding='unicode')
    with open(r'C:\Users\Engineer Jed\AndroidStudioProjects\CargoLink\app\src\main\res\drawable\icon_vector.xml', 'w', encoding='utf-8') as f:
        f.write('<?xml version="1.0" encoding="utf-8"?>\n' + xml_str)
    print('SUCCESS')
except Exception as e:
    print('ERROR:', e)
