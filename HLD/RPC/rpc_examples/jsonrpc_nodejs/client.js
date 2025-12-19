/**
 * JSON-RPC 2.0 Client Example
 * Demonstrates making JSON-RPC calls to the server
 */

const http = require('http');

/**
 * Make a JSON-RPC call
 * @param {string} method - Method name
 * @param {Array} params - Method parameters
 * @param {number} id - Request ID
 * @returns {Promise} Promise that resolves with the result
 */
function jsonRpcCall(method, params = [], id = 1) {
    return new Promise((resolve, reject) => {
        const requestData = JSON.stringify({
            jsonrpc: '2.0',
            method: method,
            params: params,
            id: id
        });

        const options = {
            hostname: 'localhost',
            port: 3000,
            path: '/',
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(requestData)
            }
        };

        const req = http.request(options, (res) => {
            let body = '';

            res.on('data', (chunk) => {
                body += chunk.toString();
            });

            res.on('end', () => {
                try {
                    const response = JSON.parse(body);

                    if (response.error) {
                        reject(new Error(
                            `RPC Error [${response.error.code}]: ${response.error.message} - ${response.error.data || ''}`
                        ));
                    } else {
                        resolve(response.result);
                    }
                } catch (error) {
                    reject(new Error(`Failed to parse response: ${error.message}`));
                }
            });
        });

        req.on('error', (error) => {
            reject(new Error(`Request failed: ${error.message}`));
        });

        req.write(requestData);
        req.end();
    });
}

/**
 * Make a batch JSON-RPC call (multiple requests in one HTTP request)
 * @param {Array} requests - Array of {method, params, id} objects
 * @returns {Promise} Promise that resolves with array of results
 */
function jsonRpcBatch(requests) {
    return new Promise((resolve, reject) => {
        const requestData = JSON.stringify(
            requests.map(req => ({
                jsonrpc: '2.0',
                method: req.method,
                params: req.params || [],
                id: req.id
            }))
        );

        const options = {
            hostname: 'localhost',
            port: 3000,
            path: '/',
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(requestData)
            }
        };

        const req = http.request(options, (res) => {
            let body = '';

            res.on('data', (chunk) => {
                body += chunk.toString();
            });

            res.on('end', () => {
                try {
                    const responses = JSON.parse(body);
                    resolve(responses);
                } catch (error) {
                    reject(new Error(`Failed to parse response: ${error.message}`));
                }
            });
        });

        req.on('error', (error) => {
            reject(new Error(`Request failed: ${error.message}`));
        });

        req.write(requestData);
        req.end();
    });
}

/**
 * Run all examples
 */
async function runExamples() {
    console.log('='.repeat(50));
    console.log('JSON-RPC 2.0 Client Examples');
    console.log('='.repeat(50));

    try {
        // Example 1: Simple addition
        console.log('\n1. Simple Addition');
        console.log('   Calling: add([5, 3])');
        const result1 = await jsonRpcCall('add', [5, 3]);
        console.log(`   Result: ${result1}`);

        // Example 2: Subtraction
        console.log('\n2. Subtraction');
        console.log('   Calling: subtract([10, 4])');
        const result2 = await jsonRpcCall('subtract', [10, 4]);
        console.log(`   Result: ${result2}`);

        // Example 3: Multiplication
        console.log('\n3. Multiplication');
        console.log('   Calling: multiply([6, 7])');
        const result3 = await jsonRpcCall('multiply', [6, 7]);
        console.log(`   Result: ${result3}`);

        // Example 4: Division
        console.log('\n4. Division');
        console.log('   Calling: divide([20, 4])');
        const result4 = await jsonRpcCall('divide', [20, 4]);
        console.log(`   Result: ${result4}`);

        // Example 5: Division by zero (error handling)
        console.log('\n5. Error Handling - Division by Zero');
        console.log('   Calling: divide([10, 0])');
        try {
            await jsonRpcCall('divide', [10, 0]);
        } catch (error) {
            console.log(`   Error caught: ${error.message}`);
        }

        // Example 6: Invalid method
        console.log('\n6. Error Handling - Invalid Method');
        console.log('   Calling: nonexistentMethod([])');
        try {
            await jsonRpcCall('nonexistentMethod', []);
        } catch (error) {
            console.log(`   Error caught: ${error.message}`);
        }

        // Example 7: Get server info
        console.log('\n7. Get Server Info');
        console.log('   Calling: getServerInfo([])');
        const serverInfo = await jsonRpcCall('getServerInfo', []);
        console.log('   Result:', JSON.stringify(serverInfo, null, 2));

        // Example 8: Echo
        console.log('\n8. Echo');
        console.log('   Calling: echo(["Hello, JSON-RPC!"])');
        const echoResult = await jsonRpcCall('echo', ['Hello, JSON-RPC!']);
        console.log(`   Result: ${echoResult}`);

        // Example 9: Batch request
        console.log('\n9. Batch Request');
        console.log('   Sending multiple requests in one HTTP call');
        const batchResults = await jsonRpcBatch([
            { method: 'add', params: [1, 2], id: 1 },
            { method: 'multiply', params: [3, 4], id: 2 },
            { method: 'subtract', params: [10, 5], id: 3 }
        ]);
        console.log('   Results:');
        batchResults.forEach((response, index) => {
            if (response.error) {
                console.log(`     Request ${response.id}: Error - ${response.error.message}`);
            } else {
                console.log(`     Request ${response.id}: ${response.result}`);
            }
        });

        console.log('\n' + '='.repeat(50));
        console.log('All examples completed successfully!');
        console.log('='.repeat(50));

    } catch (error) {
        console.error('Error running examples:', error.message);
        process.exit(1);
    }
}

// Run examples
runExamples();

