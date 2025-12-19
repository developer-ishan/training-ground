/**
 * JSON-RPC 2.0 Server Example
 * Implements a simple calculator service using JSON-RPC protocol
 */

const http = require('http');
const url = require('url');

// JSON-RPC 2.0 Error Codes
const ERROR_CODES = {
    PARSE_ERROR: -32700,
    INVALID_REQUEST: -32600,
    METHOD_NOT_FOUND: -32601,
    INVALID_PARAMS: -32602,
    INTERNAL_ERROR: -32603,
    SERVER_ERROR: -32000
};

// Service methods implementation
const methods = {
    /**
     * Add two numbers
     * @param {Array} params - [a, b]
     * @returns {number} Sum of a and b
     */
    add: (params) => {
        if (!Array.isArray(params) || params.length !== 2) {
            throw new Error('Invalid params: expected [number, number]');
        }
        const [a, b] = params;
        if (typeof a !== 'number' || typeof b !== 'number') {
            throw new Error('Invalid params: both arguments must be numbers');
        }
        return a + b;
    },

    /**
     * Subtract two numbers
     * @param {Array} params - [a, b]
     * @returns {number} Difference of a and b
     */
    subtract: (params) => {
        if (!Array.isArray(params) || params.length !== 2) {
            throw new Error('Invalid params: expected [number, number]');
        }
        const [a, b] = params;
        if (typeof a !== 'number' || typeof b !== 'number') {
            throw new Error('Invalid params: both arguments must be numbers');
        }
        return a - b;
    },

    /**
     * Multiply two numbers
     * @param {Array} params - [a, b]
     * @returns {number} Product of a and b
     */
    multiply: (params) => {
        if (!Array.isArray(params) || params.length !== 2) {
            throw new Error('Invalid params: expected [number, number]');
        }
        const [a, b] = params;
        if (typeof a !== 'number' || typeof b !== 'number') {
            throw new Error('Invalid params: both arguments must be numbers');
        }
        return a * b;
    },

    /**
     * Divide two numbers
     * @param {Array} params - [a, b]
     * @returns {number} Quotient of a and b
     */
    divide: (params) => {
        if (!Array.isArray(params) || params.length !== 2) {
            throw new Error('Invalid params: expected [number, number]');
        }
        const [a, b] = params;
        if (typeof a !== 'number' || typeof b !== 'number') {
            throw new Error('Invalid params: both arguments must be numbers');
        }
        if (b === 0) {
            throw new Error('Division by zero');
        }
        return a / b;
    },

    /**
     * Get server information
     * @param {Array} params - []
     * @returns {Object} Server info
     */
    getServerInfo: (params) => {
        return {
            name: 'JSON-RPC Calculator Server',
            version: '1.0.0',
            methods: Object.keys(methods),
            timestamp: Date.now()
        };
    },

    /**
     * Echo a message
     * @param {Array} params - [message]
     * @returns {string} Echoed message
     */
    echo: (params) => {
        if (!Array.isArray(params) || params.length !== 1) {
            throw new Error('Invalid params: expected [message]');
        }
        return `Echo: ${params[0]}`;
    }
};

/**
 * Process a single JSON-RPC request
 * @param {Object} request - JSON-RPC request object
 * @returns {Object} JSON-RPC response object
 */
function processRequest(request) {
    // Validate JSON-RPC version
    if (request.jsonrpc !== '2.0') {
        return {
            jsonrpc: '2.0',
            error: {
                code: ERROR_CODES.INVALID_REQUEST,
                message: 'Invalid Request',
                data: 'jsonrpc must be "2.0"'
            },
            id: request.id !== undefined ? request.id : null
        };
    }

    // Validate method
    if (typeof request.method !== 'string') {
        return {
            jsonrpc: '2.0',
            error: {
                code: ERROR_CODES.INVALID_REQUEST,
                message: 'Invalid Request',
                data: 'method must be a string'
            },
            id: request.id !== undefined ? request.id : null
        };
    }

    // Check if method exists
    const method = methods[request.method];
    if (!method) {
        return {
            jsonrpc: '2.0',
            error: {
                code: ERROR_CODES.METHOD_NOT_FOUND,
                message: 'Method not found',
                data: `Method "${request.method}" does not exist`
            },
            id: request.id !== undefined ? request.id : null
        };
    }

    // Execute method
    try {
        const result = method(request.params || []);
        return {
            jsonrpc: '2.0',
            result: result,
            id: request.id !== undefined ? request.id : null
        };
    } catch (error) {
        return {
            jsonrpc: '2.0',
            error: {
                code: ERROR_CODES.INVALID_PARAMS,
                message: 'Invalid params',
                data: error.message
            },
            id: request.id !== undefined ? request.id : null
        };
    }
}

/**
 * Process batch requests (array of requests)
 * @param {Array} requests - Array of JSON-RPC request objects
 * @returns {Array} Array of JSON-RPC response objects
 */
function processBatch(requests) {
    if (!Array.isArray(requests) || requests.length === 0) {
        return [{
            jsonrpc: '2.0',
            error: {
                code: ERROR_CODES.INVALID_REQUEST,
                message: 'Invalid Request',
                data: 'Batch must be a non-empty array'
            },
            id: null
        }];
    }

    return requests.map(processRequest);
}

// Create HTTP server
const server = http.createServer((req, res) => {
    // Only accept POST requests
    if (req.method !== 'POST') {
        res.writeHead(405, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
            error: 'Method Not Allowed',
            message: 'Only POST requests are accepted'
        }));
        return;
    }

    // Only accept JSON content type
    const contentType = req.headers['content-type'];
    if (!contentType || !contentType.includes('application/json')) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
            error: 'Bad Request',
            message: 'Content-Type must be application/json'
        }));
        return;
    }

    let body = '';

    // Collect request body
    req.on('data', (chunk) => {
        body += chunk.toString();
    });

    // Process request when body is complete
    req.on('end', () => {
        let request;
        let response;

        try {
            // Parse JSON
            request = JSON.parse(body);
        } catch (error) {
            // Parse error
            response = {
                jsonrpc: '2.0',
                error: {
                    code: ERROR_CODES.PARSE_ERROR,
                    message: 'Parse error',
                    data: error.message
                },
                id: null
            };
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify(response));
            return;
        }

        // Process request (single or batch)
        if (Array.isArray(request)) {
            response = processBatch(request);
        } else {
            response = processRequest(request);
        }

        // Send response
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(response));
    });

    // Handle errors
    req.on('error', (error) => {
        console.error('Request error:', error);
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
            jsonrpc: '2.0',
            error: {
                code: ERROR_CODES.INTERNAL_ERROR,
                message: 'Internal error',
                data: error.message
            },
            id: null
        }));
    });
});

// Start server
const PORT = 3000;
server.listen(PORT, () => {
    console.log('='.repeat(50));
    console.log('JSON-RPC 2.0 Server');
    console.log('='.repeat(50));
    console.log(`Server listening on port ${PORT}`);
    console.log(`Available methods: ${Object.keys(methods).join(', ')}`);
    console.log('='.repeat(50));
});

// Handle server errors
server.on('error', (error) => {
    console.error('Server error:', error);
});

