#!/bin/bash

# Название JAR-файла для поиска и перезапуска
JAR_FILE="build/libs/PersyBot.jar"

# Функция для остановки процесса
stop_process() {
  # Поиск идентификатора процесса по названию JAR-файла
  PID=$(ps -ef | grep "$JAR_FILE" | grep -v grep | awk '{print $2}')

  if [ -n "$PID" ]; then
    echo "Остановка процесса с PID: $PID"
    kill "$PID"
    sleep 2 # Дополнительное время для завершения процесса
  else
    echo "Процесс не найден"
  fi
}

# Функция для запуска процесса
start_process() {
  echo "Запуск процесса"
  nohup java -jar "$JAR_FILE" > /dev/null 2>&1 &
}

# Проверка аргумента командной строки
if [ $# -ne 1 ]; then
  echo "Использование: $0 [start|stop|restart]"
  exit 1
fi

# Выполнение операции в зависимости от переданного аргумента
case "$1" in
  "start")
    start_process
    echo "Процесс запущен"
    ;;
  "stop")
    stop_process
    echo "Процесс остановлен"
    ;;
  "restart")
    stop_process
    start_process
    echo "Процесс перезапущен"
    ;;
  *)
    echo "Неверная операция: $1"
    echo "Использование: $0 [start|stop|restart]"
    exit 1
    ;;
esac
