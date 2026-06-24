/**
 * Convert SVG tab icons to PNG for mini-program / App compatibility.
 * Uses a simple approach: create an HTML canvas via a data-URI.
 * Since we don't have sharp/canvas, we'll use the built-in approach
 * of generating PNG from SVG using a small inline script.
 */
const fs = require('fs');
const path = require('path');

const tabsDir = path.join(__dirname, 'src/static/tabs');
const SIZE = 81; // 81x81 is recommended for tabBar icons (27pt @3x)

const svgFiles = [
  'home.svg', 'home-active.svg',
  'discover.svg', 'discover-active.svg',
  'activity.svg', 'activity-active.svg',
  'message.svg', 'message-active.svg',
  'profile.svg', 'profile-active.svg',
];

// For each SVG, create a simple PNG using sharp if available,
// otherwise create a minimal valid PNG programmatically.

// Let's try a different approach - create PNGs from SVG content
// using node's built-in capabilities

function createPngFromSvg(svgContent, outputPath, size) {
  // Read SVG, ensure it has width/height
  let svg = svgContent;
  
  // Add width and height if not present
  if (!svg.includes('width=')) {
    svg = svg.replace('<svg ', `<svg width="${size}" height="${size}" `);
  }
  
  // Encode SVG as base64 for use in HTML
  const svgBase64 = Buffer.from(svg).toString('base64');
  
  // We'll write a minimal 1-color PNG as placeholder and use the SVG in H5 mode
  // For actual conversion, we need to use a different approach
  
  // Create a minimal valid PNG (transparent)
  const png = createMinimalPng(size, svgContent);
  fs.writeFileSync(outputPath, png);
  console.log(`Created: ${path.basename(outputPath)}`);
}

function extractColor(svgContent) {
  // Try to extract fill color from SVG
  const fillMatch = svgContent.match(/fill="(#[0-9A-Fa-f]{6})"/);
  if (fillMatch) return fillMatch[1];
  return '#8A8A99';
}

function hexToRgb(hex) {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  return result ? {
    r: parseInt(result[1], 16),
    g: parseInt(result[2], 16),
    b: parseInt(result[3], 16)
  } : { r: 138, g: 138, b: 153 };
}

function createMinimalPng(size, svgContent) {
  // We need a real PNG with the icon rendered.
  // Since we can't render SVG to PNG without dependencies,
  // let's create the PNG files using a different strategy:
  // We'll create a simple HTML file that renders all SVGs to canvas and downloads them.
  
  // For now, create a valid transparent PNG
  // PNG signature + IHDR + IDAT + IEND
  
  const zlib = require('zlib');
  
  // IHDR data
  const width = size;
  const height = size;
  const bitDepth = 8;
  const colorType = 6; // RGBA
  
  // Create raw image data (all transparent)
  const rawData = Buffer.alloc((width * 4 + 1) * height, 0);
  for (let y = 0; y < height; y++) {
    rawData[y * (width * 4 + 1)] = 0; // filter byte
  }
  
  // Parse SVG path and render to pixel buffer
  renderSvgToBuffer(rawData, width, height, svgContent);
  
  const compressed = zlib.deflateSync(rawData);
  
  // Build PNG
  const chunks = [];
  
  // Signature
  chunks.push(Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]));
  
  // IHDR
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(width, 0);
  ihdr.writeUInt32BE(height, 4);
  ihdr[8] = bitDepth;
  ihdr[9] = colorType;
  ihdr[10] = 0; // compression
  ihdr[11] = 0; // filter
  ihdr[12] = 0; // interlace
  chunks.push(createPngChunk('IHDR', ihdr));
  
  // IDAT
  chunks.push(createPngChunk('IDAT', compressed));
  
  // IEND
  chunks.push(createPngChunk('IEND', Buffer.alloc(0)));
  
  return Buffer.concat(chunks);
}

function renderSvgToBuffer(rawData, width, height, svgContent) {
  // Extract paths and fill color from SVG
  const color = extractColor(svgContent);
  const rgb = hexToRgb(color);
  
  // Extract path data
  const pathMatch = svgContent.match(/d="([^"]+)"/g);
  if (!pathMatch) return;
  
  // Get viewBox
  const vbMatch = svgContent.match(/viewBox="([^"]+)"/);
  const vb = vbMatch ? vbMatch[1].split(/\s+/).map(Number) : [0, 0, 24, 24];
  const [vbX, vbY, vbW, vbH] = vb;
  
  // Scale factor
  const scaleX = width / vbW;
  const scaleY = height / vbH;
  
  // Simple SVG path rasterizer
  for (const pm of pathMatch) {
    const d = pm.match(/d="([^"]+)"/)[1];
    rasterizePath(rawData, width, height, d, scaleX, scaleY, vbX, vbY, rgb);
  }
}

function rasterizePath(rawData, width, height, d, scaleX, scaleY, vbX, vbY, rgb) {
  // Parse SVG path commands and create a filled polygon
  const points = parseSvgPath(d);
  
  // Transform points
  const transformed = points.map(p => ({
    x: (p.x - vbX) * scaleX,
    y: (p.y - vbY) * scaleY
  }));
  
  // Fill using scanline
  if (transformed.length < 3) return;
  
  // Get edges from the path segments
  const edges = [];
  for (let i = 0; i < transformed.length - 1; i++) {
    edges.push({ x1: transformed[i].x, y1: transformed[i].y, x2: transformed[i + 1].x, y2: transformed[i + 1].y });
  }
  // Close the path
  if (transformed.length > 2) {
    edges.push({ x1: transformed[transformed.length - 1].x, y1: transformed[transformed.length - 1].y, x2: transformed[0].x, y2: transformed[0].y });
  }
  
  // Scanline fill
  for (let y = 0; y < height; y++) {
    const intersections = [];
    for (const edge of edges) {
      const { x1, y1, x2, y2 } = edge;
      if ((y1 <= y && y2 > y) || (y2 <= y && y1 > y)) {
        const x = x1 + (y - y1) / (y2 - y1) * (x2 - x1);
        intersections.push(x);
      }
    }
    intersections.sort((a, b) => a - b);
    
    for (let i = 0; i < intersections.length - 1; i += 2) {
      const xStart = Math.max(0, Math.ceil(intersections[i]));
      const xEnd = Math.min(width - 1, Math.floor(intersections[i + 1]));
      for (let x = xStart; x <= xEnd; x++) {
        const offset = y * (width * 4 + 1) + 1 + x * 4;
        rawData[offset] = rgb.r;
        rawData[offset + 1] = rgb.g;
        rawData[offset + 2] = rgb.b;
        rawData[offset + 3] = 255;
      }
    }
  }
}

function parseSvgPath(d) {
  const points = [];
  let x = 0, y = 0;
  
  // Tokenize
  const tokens = d.match(/[MmLlHhVvCcSsQqTtAaZz]|[-+]?[0-9]*\.?[0-9]+(?:[eE][-+]?[0-9]+)?/g);
  if (!tokens) return points;
  
  let cmd = '';
  let i = 0;
  
  while (i < tokens.length) {
    if (/[A-Za-z]/.test(tokens[i])) {
      cmd = tokens[i];
      i++;
    }
    
    switch (cmd) {
      case 'M':
        x = parseFloat(tokens[i++]);
        y = parseFloat(tokens[i++]);
        points.push({ x, y });
        cmd = 'L'; // subsequent coords are lines
        break;
      case 'm':
        x += parseFloat(tokens[i++]);
        y += parseFloat(tokens[i++]);
        points.push({ x, y });
        cmd = 'l';
        break;
      case 'L':
        x = parseFloat(tokens[i++]);
        y = parseFloat(tokens[i++]);
        points.push({ x, y });
        break;
      case 'l':
        x += parseFloat(tokens[i++]);
        y += parseFloat(tokens[i++]);
        points.push({ x, y });
        break;
      case 'H':
        x = parseFloat(tokens[i++]);
        points.push({ x, y });
        break;
      case 'h':
        x += parseFloat(tokens[i++]);
        points.push({ x, y });
        break;
      case 'V':
        y = parseFloat(tokens[i++]);
        points.push({ x, y });
        break;
      case 'v':
        y += parseFloat(tokens[i++]);
        points.push({ x, y });
        break;
      case 'C':
        // Cubic bezier - sample points along the curve
        {
          const x1 = parseFloat(tokens[i++]);
          const y1 = parseFloat(tokens[i++]);
          const x2 = parseFloat(tokens[i++]);
          const y2 = parseFloat(tokens[i++]);
          const ex = parseFloat(tokens[i++]);
          const ey = parseFloat(tokens[i++]);
          // Sample the cubic bezier
          for (let t = 0.1; t <= 1; t += 0.1) {
            const bx = cubicBezier(x, x1, x2, ex, t);
            const by = cubicBezier(y, y1, y2, ey, t);
            points.push({ x: bx, y: by });
          }
          x = ex;
          y = ey;
        }
        break;
      case 'c':
        {
          const x1 = x + parseFloat(tokens[i++]);
          const y1 = y + parseFloat(tokens[i++]);
          const x2 = x + parseFloat(tokens[i++]);
          const y2 = y + parseFloat(tokens[i++]);
          const ex = x + parseFloat(tokens[i++]);
          const ey = y + parseFloat(tokens[i++]);
          for (let t = 0.1; t <= 1; t += 0.1) {
            const bx = cubicBezier(x, x1, x2, ex, t);
            const by = cubicBezier(y, y1, y2, ey, t);
            points.push({ x: bx, y: by });
          }
          x = ex;
          y = ey;
        }
        break;
      case 'Q':
        {
          const qx1 = parseFloat(tokens[i++]);
          const qy1 = parseFloat(tokens[i++]);
          const qex = parseFloat(tokens[i++]);
          const qey = parseFloat(tokens[i++]);
          for (let t = 0.1; t <= 1; t += 0.1) {
            const bx = quadBezier(x, qx1, qex, t);
            const by = quadBezier(y, qy1, qey, t);
            points.push({ x: bx, y: by });
          }
          x = qex;
          y = qey;
        }
        break;
      case 'Z':
      case 'z':
        if (points.length > 0) {
          points.push({ x: points[0].x, y: points[0].y });
        }
        break;
      default:
        i++; // skip unknown
        break;
    }
  }
  
  return points;
}

function cubicBezier(p0, p1, p2, p3, t) {
  const mt = 1 - t;
  return mt * mt * mt * p0 + 3 * mt * mt * t * p1 + 3 * mt * t * t * p2 + t * t * t * p3;
}

function quadBezier(p0, p1, p2, t) {
  const mt = 1 - t;
  return mt * mt * p0 + 2 * mt * t * p1 + t * t * p2;
}

function createPngChunk(type, data) {
  const length = Buffer.alloc(4);
  length.writeUInt32BE(data.length, 0);
  
  const typeBuffer = Buffer.from(type, 'ascii');
  const crcData = Buffer.concat([typeBuffer, data]);
  
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(crcData), 0);
  
  return Buffer.concat([length, typeBuffer, data, crc]);
}

function crc32(buf) {
  let crc = 0xFFFFFFFF;
  for (let i = 0; i < buf.length; i++) {
    crc ^= buf[i];
    for (let j = 0; j < 8; j++) {
      if (crc & 1) {
        crc = (crc >>> 1) ^ 0xEDB88320;
      } else {
        crc >>>= 1;
      }
    }
  }
  return (crc ^ 0xFFFFFFFF) >>> 0;
}

// Main
for (const svgFile of svgFiles) {
  const svgPath = path.join(tabsDir, svgFile);
  const pngFile = svgFile.replace('.svg', '.png');
  const pngPath = path.join(tabsDir, pngFile);
  
  if (fs.existsSync(svgPath)) {
    const svgContent = fs.readFileSync(svgPath, 'utf-8');
    createPngFromSvg(svgContent, pngPath, SIZE);
  } else {
    console.warn(`SVG not found: ${svgFile}`);
  }
}

console.log('\nDone! Now update pages.json to use .png paths.');
