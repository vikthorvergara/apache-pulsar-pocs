#!/bin/bash

set -e

echo "Setting up Apache Pulsar namespaces..."

# Wait for Pulsar to be ready
echo "Waiting for Pulsar to be healthy..."
until docker exec pulsar bin/pulsar-admin brokers healthcheck &>/dev/null; do
    echo "   Waiting for Pulsar to start..."
    sleep 2
done
echo "Pulsar is healthy!"

# Create tenants
echo ""
echo "Creating tenants..."
for tenant in healthcare finance retail; do
    if docker exec pulsar bin/pulsar-admin tenants list | grep -q "^${tenant}$"; then
        echo "   Tenant '${tenant}' already exists"
    else
        docker exec pulsar bin/pulsar-admin tenants create ${tenant}
        echo "   Created tenant: ${tenant}"
    fi
done

# Create namespaces
echo ""
echo "Creating namespaces..."
for tenant in healthcare finance retail; do
    namespace="${tenant}/events"
    if docker exec pulsar bin/pulsar-admin namespaces list ${tenant} 2>/dev/null | grep -q "${namespace}"; then
        echo "   Namespace '${namespace}' already exists"
    else
        docker exec pulsar bin/pulsar-admin namespaces create ${namespace}
        echo "   Created namespace: ${namespace}"
    fi
done

# List created resources
echo ""
echo "Verification:"
echo "   Tenants:"
docker exec pulsar bin/pulsar-admin tenants list | grep -E "(healthcare|finance|retail)" | sed 's/^/      - /'

echo "   Namespaces:"
for tenant in healthcare finance retail; do
    docker exec pulsar bin/pulsar-admin namespaces list ${tenant} 2>/dev/null | sed 's/^/      - /'
done

echo ""
echo "Pulsar setup complete!"
echo "   You can now run the application with: ./mvnw spring-boot:run"
