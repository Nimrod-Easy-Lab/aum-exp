# aum-exp
Avoiding Useless Mutants - Experiment [Strategy to help identifying new RULES] 

Getting started
----------------
#### Setting up 
1. Clone aum-exp:
    - `git clone https://github.com/Nimrod-Easy-Lab/aum-exp.git`

2. Initialize aum-exp (download the project repositories and external libraries, which are not included in the git repository for size purposes and to avoid redundancies):
    - `cd aum-exp`
    - `sh build_script.sh`

3. Add ...

#### Using aum-exp
4. Executing
    - `java -jar target/...jar <path to data>`


#### Developing aum-exp
4. Eclipse IDE 
    - Install AspectJ [AJDT]: https://www.eclipse.org/ajdt/downloads/ (Get a build specific for your Eclipse version)
    - In Eclipse: File > Import. Select: Existing Maven Projects. Browse to the root directory of aum-exp
    - If there is an error in pom.xml, then open it, click in the error message and install the missing plugins
    - Open Run Configurations and add a new AspectJ/Java Application (the main class is emp_experiment.Main). You need to specify the path to the subject/input folder in the Program Arguments.
    - ...


Publications
------------------
* "Avoiding Useless Mutants"
    GPCE 2017 [gpce17].
    
[gpce17]: https://doi.org/10.1145/3170492.3136053