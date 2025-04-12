#!/bin/bash
# setup-dev.sh
# This script helps set up the development environment for the Transaction Ingestion Service

# Color codes for output formatting
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}Setting up development environment for Transaction Ingestion Service${NC}"
echo

# Check if Java is installed
echo -e "${YELLOW}Checking Java installation...${NC}"
if type -p java > /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed 's/^1\.//' | cut -d'.' -f1)
    if [[ "${JAVA_VERSION}" -ge "17" ]]; then
        echo -e "${GREEN}✓ Java ${JAVA_VERSION} found${NC}"
    else
        echo -e "${RED}✗ Java ${JAVA_VERSION} found, but version 17 or higher is required${NC}"
        exit 1
    fi
else
    echo -e "${RED}✗ Java not found. Please install JDK 17 or higher${NC}"
    exit 1
fi

# Check if Maven is installed
echo -e "${YELLOW}Checking Maven installation...${NC}"
if type -p mvn > /dev/null; then
    MVN_VERSION=$(mvn --version | head -1 | cut -d' ' -f3)
    echo -e "${GREEN}✓ Maven ${MVN_VERSION} found${NC}"
else
    echo -e "${RED}✗ Maven not found. Please install Maven 3.6 or higher${NC}"
    exit 1
fi

# Generate Avro sources
echo -e "${YELLOW}Generating Avro sources...${NC}"
mvn clean generate-sources
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Avro sources generated successfully${NC}"
else
    echo -e "${RED}✗ Failed to generate Avro sources${NC}"
    exit 1
fi

# Check if target/generated-sources/avro directory exists
if [ -d "target/generated-sources/avro" ]; then
    # Count generated classes
    CLASS_COUNT=$(find target/generated-sources/avro -name "*.java" | wc -l)
    echo -e "${GREEN}✓ ${CLASS_COUNT} Avro classes generated${NC}"
else
    echo -e "${RED}✗ No Avro classes generated${NC}"
    exit 1
fi

# Suggest IDE settings
echo
echo -e "${YELLOW}IDE Configuration:${NC}"
echo -e "Make sure your IDE recognizes 'target/generated-sources/avro' as a source directory."
echo
echo -e "For IntelliJ IDEA:"
echo -e "  1. Go to File > Project Structure > Modules"
echo -e "  2. Find the target/generated-sources/avro directory"
echo -e "  3. Mark it as 'Sources'"
echo
echo -e "For Eclipse:"
echo -e "  1. Right-click on the project > Properties"
echo -e "  2. Go to Java Build Path > Source tab"
echo -e "  3. Add the target/generated-sources/avro folder"
echo

# Final success message
echo -e "${GREEN}Setup complete! You're ready to start developing.${NC}"
echo
echo -e "Run the application with: ${YELLOW}mvn spring-boot:run${NC}"
echo -e "Build the application with: ${YELLOW}mvn clean package${NC}"
echo