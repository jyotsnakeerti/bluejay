package blue;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.poi.ss.usermodel.*;

public class EmployeeAnalyzer {
    
	static class Employee {
	    String name;
	    String positionId; 
	    Date timeIn;
	    Date timeOut;

	    public Employee(String name, String positionId, Date timeIn, Date timeOut) {
	        this.name = name;
	        this.positionId = positionId; 
	        this.timeIn = timeIn;
	        this.timeOut = timeOut;
	    }
	}

    public static List<Employee> parseExcelFile(File file) throws IOException, ParseException {
        List<Employee> employees = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");

        try (Workbook workbook = WorkbookFactory.create(new FileInputStream(file))) {
            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                String name = null;
                Cell nameCell = row.getCell(7); // Assuming Employee Name is at index 7
                if (nameCell != null) {
                    if (nameCell.getCellType() == CellType.STRING) {
                        name = nameCell.getStringCellValue();
                    } else if (nameCell.getCellType() == CellType.NUMERIC) {
                        // Handle numeric value as string
                        name = String.valueOf((int) nameCell.getNumericCellValue());
                    }
                }
                
                // Extract position ID from the appropriate column (e.g., assuming Position ID is at index 0)
                String positionId = null;
                Cell positionIdCell = row.getCell(0); // Assuming Position ID is at index 0
                if (positionIdCell != null) {
                    if (positionIdCell.getCellType() == CellType.STRING) {
                        positionId = positionIdCell.getStringCellValue();
                    } else if (positionIdCell.getCellType() == CellType.NUMERIC) {
                        positionId = String.valueOf((int) positionIdCell.getNumericCellValue());
                    }
                }
                
                Date timeIn = null;
                Cell timeInCell = row.getCell(2); // Assuming Time In is at index 2
                if (timeInCell != null && timeInCell.getCellType() == CellType.NUMERIC) {
                    timeIn = timeInCell.getDateCellValue();
                }

                Date timeOut = null;
                Cell timeOutCell = row.getCell(3); // Assuming Time Out is at index 3
                if (timeOutCell != null && timeOutCell.getCellType() == CellType.NUMERIC) {
                    timeOut = timeOutCell.getDateCellValue();
                }

                employees.add(new Employee(name, positionId, timeIn, timeOut)); // Assign position ID to Employee
            }
        }
        return employees;
    }


    public static void findEmployeesWithConsecutiveDays(List<Employee> employees) {
        // Assuming employees are sorted by name and then by time in
        Collections.sort(employees, Comparator.comparing((Employee e) -> e.name)
                .thenComparing(e -> e.timeIn));

        Map<String, Integer> consecutiveDays = new HashMap<>();
        for (Employee employee : employees) {
            consecutiveDays.put(employee.name, consecutiveDays.getOrDefault(employee.name, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : consecutiveDays.entrySet()) {
            if (entry.getValue() >= 7) {
                // Retrieve position ID of the current employee
                String positionId = null;
                for (Employee employee : employees) {
                    if (employee.name.equals(entry.getKey())) {
                        positionId = employee.positionId;
                        break;
                    }
                }
                System.out.println(entry.getKey() + " (" + positionId + ") worked for 7 consecutive days");
            }
        }
    }




    public static void findEmployeesWithShortBreaks(List<Employee> employees) {
        for (int i = 0; i < employees.size() - 1; i++) {
            Employee current = employees.get(i);
            Employee next = employees.get(i + 1);
            
            // Check if both current and next employees have non-null timeIn values
            if (current.timeOut != null && next.timeIn != null) {
                long diffInMillies = next.timeIn.getTime() - current.timeOut.getTime();
                long diffInHours = diffInMillies / (60 * 60 * 1000);

                if (diffInHours > 1 && diffInHours < 10) {
                    System.out.println(current.name + " (" + current.positionId + ") has less than 10 hours between shifts");

                    // Move the index to the next employee to avoid considering this shift pair again
                    i++;
                }
            }
        }
    }


    public static void findEmployeesWithLongShifts(List<Employee> employees) {
        for (Employee employee : employees) {
            if (employee.timeOut != null && employee.timeIn != null) {
                long diffInMillies = employee.timeOut.getTime() - employee.timeIn.getTime();
                long diffInHours = diffInMillies / (60 * 60 * 1000);

                if (diffInHours > 14) {
                    System.out.println(employee.name + " (" + employee.positionId + ") worked for more than 14 hours in a single shift");
                }
            }
        }
    }


    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java EmployeeAnalyzer Assignment_timecard.xlsx");
            System.exit(1);
        }

        File inputFile = new File(args[0]);
        try {
            List<Employee> employees = parseExcelFile(inputFile);
            findEmployeesWithConsecutiveDays(employees);
            System.out.println();
            findEmployeesWithShortBreaks(employees);
            System.out.println();
            findEmployeesWithLongShifts(employees);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
