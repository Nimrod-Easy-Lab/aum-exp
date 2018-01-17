# aum-exp
Avoiding Useless Mutants - Experiment [Strategy to help identifying new RULES] 

## Execute
$> 
$> java -jar target/...


Getting started
----------------
#### Setting up 
1. Clone aum-exp:
    - `git clone https://github.com/Nimrod-Easy-Lab/aum-exp.git`

2. Initialize Defects4J (download the project repositories and external libraries, which are not included in the git repository for size purposes and to avoid redundancies):
    - `cd aum-exp`
    - `sh build_script.sh`

3. Add ...

#### Using aum-exp
4. Check installation and get information for a specific project (commons lang):
    - `defects4j info -p Lang`

Publications
------------------
* "Avoiding Useless Mutants"
    GPCE 2017 [gpce17].

* "Are Mutants a Valid Substitute for Real Faults in Software Testing?"
    René Just, Darioush Jalali, Laura Inozemtseva, Michael D. Ernst, Reid Holmes, and Gordon Fraser,
    FSE 2014 [[download]][fse14].

[gpce17]: https://doi.org/10.1145/3170492.3136053