How the Bridge Design Pattern Works 🔧
1. Abstraction: Define the high-level interface for shapes.

2. Implementor: Define the interface for rendering methods.

3. Concrete Implementor: Provide specific implementations of the rendering methods (e.g., Raster, Vector).

4. Refined Abstraction: Implement shapes by using rendering methods through composition.

# Bridge Pattern decouples what is being done (shapes) from how it is being done (rendering methods)